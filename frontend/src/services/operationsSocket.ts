import { useState, useEffect } from 'react';
import { useQueryClient } from '@tanstack/react-query';
import { OperationsEvent } from '@/types';
import { getAuthToken } from './api';

export type SocketStatus = 'LIVE' | 'RECONNECTING' | 'OFFLINE';

type EventHandler = (event: OperationsEvent) => void;

class OperationsSocketManager {
  private socket: WebSocket | null = null;
  private status: SocketStatus = 'OFFLINE';
  private listeners: Set<EventHandler> = new Set();
  private statusListeners: Set<(status: SocketStatus) => void> = new Set();
  private reconnectTimeout: any = null;
  private reconnectAttempts = 0;
  private maxReconnectAttempts = 15;
  private isExplicitlyClosed = false;

  constructor() {
    // Lazy connect on initialization
    if (typeof window !== 'undefined') {
      this.connect();
    }
  }

  public getStatus(): SocketStatus {
    return this.status;
  }

  private setStatus(newStatus: SocketStatus) {
    this.status = newStatus;
    this.statusListeners.forEach((cb) => {
      try {
        cb(newStatus);
      } catch (e) {
        console.error('Error in socket status listener:', e);
      }
    });
  }

  public connect() {
    const token = getAuthToken();
    if (!token) {
      this.setStatus('OFFLINE');
      return;
    }

    if (this.socket && (this.socket.readyState === WebSocket.OPEN || this.socket.readyState === WebSocket.CONNECTING)) {
      return;
    }

    this.isExplicitlyClosed = false;
    this.setStatus('RECONNECTING');

    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    // In local Vite dev mode, default directly to localhost:8082 unless proxied;
    // in production/container build, route through Nginx reverse proxy via window.location.host
    const isDev = Boolean((import.meta as any).env?.DEV);
    const host = isDev && window.location.port === '5173'
      ? 'localhost:8082'
      : window.location.host;
    const wsUrl = `${protocol}//${host}/ws/websocket`;

    try {
      this.socket = new WebSocket(wsUrl);

      this.socket.onopen = () => {
        this.reconnectAttempts = 0;
        // Send STOMP CONNECT frame with Bearer token
        const connectHeaders = [
          'accept-version:1.2,1.1,1.0',
          'heart-beat:10000,10000',
          `Authorization:Bearer ${token}`,
        ];
        const connectFrame = `CONNECT\n${connectHeaders.join('\n')}\n\n\0`;
        this.socket?.send(connectFrame);
      };

      this.socket.onmessage = (event) => {
        const raw = event.data;
        if (typeof raw !== 'string') return;

        // Parse STOMP frames
        if (raw.startsWith('ERROR')) {
          console.warn('[OperationsSocket] Received STOMP ERROR frame from server:', raw);
          this.setStatus('OFFLINE');
          return;
        }

        if (raw.startsWith('CONNECTED')) {
          this.setStatus('LIVE');
          // Subscribe to topics
          const subOps = `SUBSCRIBE\nid:sub-ops-0\ndestination:/topic/operations\n\n\0`;
          const subAlerts = `SUBSCRIBE\nid:sub-alerts-1\ndestination:/topic/alerts\n\n\0`;
          this.socket?.send(subOps);
          this.socket?.send(subAlerts);
          return;
        }

        if (raw.startsWith('MESSAGE')) {
          // Extract body after double newline and before null terminator \0
          const bodyIndex = raw.indexOf('\n\n');
          if (bodyIndex !== -1) {
            let body = raw.substring(bodyIndex + 2);
            if (body.endsWith('\0')) {
              body = body.slice(0, -1);
            }
            try {
              const parsed: OperationsEvent = JSON.parse(body.trim());
              this.notifyListeners(parsed);
            } catch (err) {
              console.warn('[OperationsSocket] Failed to parse event body:', err, body);
            }
          }
        }
      };

      this.socket.onerror = () => {
        // Socket error handled in onclose
      };

      this.socket.onclose = () => {
        this.socket = null;
        if (!this.isExplicitlyClosed) {
          this.setStatus('RECONNECTING');
          this.scheduleReconnect();
        } else {
          this.setStatus('OFFLINE');
        }
      };
    } catch (err) {
      console.warn('[OperationsSocket] Connect failure:', err);
      this.scheduleReconnect();
    }
  }

  private scheduleReconnect() {
    if (this.reconnectTimeout) clearTimeout(this.reconnectTimeout);

    if (this.reconnectAttempts >= this.maxReconnectAttempts) {
      this.setStatus('OFFLINE');
      return;
    }

    const backoff = Math.min(15000, Math.pow(1.5, this.reconnectAttempts) * 1500);
    this.reconnectAttempts++;

    this.reconnectTimeout = setTimeout(() => {
      this.connect();
    }, backoff);
  }

  private notifyListeners(event: OperationsEvent) {
    this.listeners.forEach((listener) => {
      try {
        listener(event);
      } catch (err) {
        console.error('Error invoking operations event listener:', err);
      }
    });
  }

  public onEvent(callback: EventHandler): () => void {
    this.listeners.add(callback);
    return () => {
      this.listeners.delete(callback);
    };
  }

  public onStatus(callback: (status: SocketStatus) => void): () => void {
    this.statusListeners.add(callback);
    callback(this.status);
    return () => {
      this.statusListeners.delete(callback);
    };
  }

  public disconnect() {
    this.isExplicitlyClosed = true;
    if (this.reconnectTimeout) clearTimeout(this.reconnectTimeout);
    if (this.socket) {
      this.socket.close();
      this.socket = null;
    }
    this.setStatus('OFFLINE');
  }
}

export const operationsSocketManager = new OperationsSocketManager();

export function useOperationsSocket() {
  const queryClient = useQueryClient();
  const [status, setStatus] = useState<SocketStatus>(operationsSocketManager.getStatus());
  const [lastEvent, setLastEvent] = useState<OperationsEvent | null>(null);
  const [recentEvents, setRecentEvents] = useState<OperationsEvent[]>([]);

  useEffect(() => {
    const unsubStatus = operationsSocketManager.onStatus(setStatus);
    const unsubEvents = operationsSocketManager.onEvent((event) => {
      setLastEvent(event);
      setRecentEvents((prev) => [event, ...prev.slice(0, 19)]);

      // Intelligent cache invalidation across React Query
      switch (event.eventType) {
        case 'WORK_ORDER_ASSIGNED':
        case 'WORK_ORDER_STATUS_CHANGED':
        case 'SLA_RISK_CHANGED':
          queryClient.invalidateQueries({ queryKey: ['dashboard-overview'] });
          queryClient.invalidateQueries({ queryKey: ['work-orders'] });
          queryClient.invalidateQueries({ queryKey: ['dashboard-work-orders'] });
          queryClient.invalidateQueries({ queryKey: ['technicians'] });
          queryClient.invalidateQueries({ queryKey: ['intelligence-overview'] });
          break;
        case 'TECHNICIAN_STATUS_CHANGED':
        case 'TECHNICIAN_LOCATION_UPDATED':
          queryClient.invalidateQueries({ queryKey: ['technicians'] });
          queryClient.invalidateQueries({ queryKey: ['dashboard-overview'] });
          break;
        case 'NEW_ALERT':
        case 'ALERT_ACKNOWLEDGED':
        case 'ALERT_RESOLVED':
          queryClient.invalidateQueries({ queryKey: ['active-alerts'] });
          queryClient.invalidateQueries({ queryKey: ['all-alerts'] });
          queryClient.invalidateQueries({ queryKey: ['dashboard-overview'] });
          queryClient.invalidateQueries({ queryKey: ['intelligence-overview'] });
          queryClient.invalidateQueries({ queryKey: ['notifications'] });
          break;
        case 'ROUTE_OPTIMIZED':
        case 'ROUTE_APPLIED':
          queryClient.invalidateQueries({ queryKey: ['optimized-route'] });
          queryClient.invalidateQueries({ queryKey: ['technicians'] });
          queryClient.invalidateQueries({ queryKey: ['dashboard-overview'] });
          queryClient.invalidateQueries({ queryKey: ['dashboard-work-orders'] });
          break;
        case 'PREDICTIVE_ALERT_CREATED':
        case 'PREDICTIVE_RISK_CHANGED':
        case 'SLA_BREACH_PREDICTED':
          queryClient.invalidateQueries({ queryKey: ['predictive-sla'] });
          queryClient.invalidateQueries({ queryKey: ['dashboard-overview'] });
          queryClient.invalidateQueries({ queryKey: ['intelligence-overview'] });
          queryClient.invalidateQueries({ queryKey: ['alerts'] });
          break;
        case 'STOCKOUT_PREDICTED':
          queryClient.invalidateQueries({ queryKey: ['inventory-predictions'] });
          queryClient.invalidateQueries({ queryKey: ['inventory'] });
          queryClient.invalidateQueries({ queryKey: ['alerts'] });
          break;
        case 'SCENARIO_APPLIED':
          queryClient.invalidateQueries({ queryKey: ['dashboard-overview'] });
          queryClient.invalidateQueries({ queryKey: ['dashboard-work-orders'] });
          queryClient.invalidateQueries({ queryKey: ['technicians'] });
          queryClient.invalidateQueries({ queryKey: ['intelligence-overview'] });
          queryClient.invalidateQueries({ queryKey: ['scenario-audits'] });
          break;
        default:
          queryClient.invalidateQueries({ queryKey: ['dashboard-overview'] });
          break;
      }
    });

    return () => {
      unsubStatus();
      unsubEvents();
    };
  }, [queryClient]);

  return {
    status,
    isLive: status === 'LIVE',
    lastEvent,
    recentEvents,
    reconnect: () => operationsSocketManager.connect(),
  };
}
