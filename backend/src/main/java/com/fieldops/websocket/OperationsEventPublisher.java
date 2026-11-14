package com.fieldops.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class OperationsEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(OperationsEventPublisher.class);
    public static final String OPERATIONS_TOPIC = "/topic/operations";
    public static final String ALERTS_TOPIC = "/topic/alerts";

    private final SimpMessagingTemplate messagingTemplate;

    public OperationsEventPublisher(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void publish(OperationsEvent event) {
        if (event == null) return;
        try {
            log.info("[WebSocket] Publishing operations event: {} [Severity: {}] - {}",
                    event.getEventType(), event.getSeverity(), event.getMessage());
            messagingTemplate.convertAndSend(OPERATIONS_TOPIC, event);

            // Also broadcast to /topic/alerts if event relates to alerts
            if (event.getEventType() == OperationsEventType.NEW_ALERT
                    || event.getEventType() == OperationsEventType.ALERT_ACKNOWLEDGED
                    || event.getEventType() == OperationsEventType.ALERT_RESOLVED) {
                messagingTemplate.convertAndSend(ALERTS_TOPIC, event);
            }
        } catch (Exception ex) {
            log.warn("[WebSocket] Failed to publish operational event to WebSocket: {}", ex.getMessage());
        }
    }
}
