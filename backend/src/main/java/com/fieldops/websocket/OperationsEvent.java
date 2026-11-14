package com.fieldops.websocket;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class OperationsEvent {

    private OperationsEventType eventType;
    private String severity;
    private String message;
    private Long workOrderId;
    private Long technicianId;
    private Long alertId;
    private LocalDateTime timestamp;
    private Map<String, Object> payload;

    public OperationsEvent() {
        this.timestamp = LocalDateTime.now();
    }

    public OperationsEvent(OperationsEventType eventType, String severity, String message,
                           Long workOrderId, Long technicianId, Long alertId,
                           Map<String, Object> payload) {
        this.eventType = eventType;
        this.severity = severity != null ? severity : "INFO";
        this.message = message;
        this.workOrderId = workOrderId;
        this.technicianId = technicianId;
        this.alertId = alertId;
        this.timestamp = LocalDateTime.now();
        this.payload = payload;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private OperationsEventType eventType;
        private String severity = "INFO";
        private String message;
        private Long workOrderId;
        private Long technicianId;
        private Long alertId;
        private LocalDateTime timestamp = LocalDateTime.now();
        private Map<String, Object> payload;

        public Builder eventType(OperationsEventType eventType) {
            this.eventType = eventType;
            return this;
        }

        public Builder severity(String severity) {
            this.severity = severity;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder workOrderId(Long workOrderId) {
            this.workOrderId = workOrderId;
            return this;
        }

        public Builder technicianId(Long technicianId) {
            this.technicianId = technicianId;
            return this;
        }

        public Builder alertId(Long alertId) {
            this.alertId = alertId;
            return this;
        }

        public Builder timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder payload(Map<String, Object> payload) {
            this.payload = payload;
            return this;
        }

        public OperationsEvent build() {
            OperationsEvent event = new OperationsEvent();
            event.eventType = this.eventType;
            event.severity = this.severity;
            event.message = this.message;
            event.workOrderId = this.workOrderId;
            event.technicianId = this.technicianId;
            event.alertId = this.alertId;
            event.timestamp = this.timestamp;
            event.payload = this.payload;
            return event;
        }
    }

    public OperationsEventType getEventType() {
        return eventType;
    }

    public void setEventType(OperationsEventType eventType) {
        this.eventType = eventType;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getWorkOrderId() {
        return workOrderId;
    }

    public void setWorkOrderId(Long workOrderId) {
        this.workOrderId = workOrderId;
    }

    public Long getTechnicianId() {
        return technicianId;
    }

    public void setTechnicianId(Long technicianId) {
        this.technicianId = technicianId;
    }

    public Long getAlertId() {
        return alertId;
    }

    public void setAlertId(Long alertId) {
        this.alertId = alertId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Map<String, Object> getPayload() {
        return payload;
    }

    public void setPayload(Map<String, Object> payload) {
        this.payload = payload;
    }
}
