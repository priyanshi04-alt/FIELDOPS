package com.fieldops.notification;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 1000)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationSeverity severity = NotificationSeverity.INFO;

    private boolean readStatus = false;
    private String relatedEntityType;
    private Long relatedEntityId;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    public Notification() {}

    public Notification(Long id, String title, String message, NotificationSeverity severity, boolean readStatus,
                        String relatedEntityType, Long relatedEntityId) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.severity = severity != null ? severity : NotificationSeverity.INFO;
        this.readStatus = readStatus;
        this.relatedEntityType = relatedEntityType;
        this.relatedEntityId = relatedEntityId;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private String title;
        private String message;
        private NotificationSeverity severity = NotificationSeverity.INFO;
        private boolean readStatus = false;
        private String relatedEntityType;
        private Long relatedEntityId;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder title(String title) { this.title = title; return this; }
        public Builder message(String message) { this.message = message; return this; }
        public Builder severity(NotificationSeverity severity) { this.severity = severity; return this; }
        public Builder readStatus(boolean readStatus) { this.readStatus = readStatus; return this; }
        public Builder relatedEntityType(String type) { this.relatedEntityType = type; return this; }
        public Builder relatedEntityId(Long id) { this.relatedEntityId = id; return this; }

        public Notification build() {
            return new Notification(id, title, message, severity, readStatus, relatedEntityType, relatedEntityId);
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public NotificationSeverity getSeverity() { return severity; }
    public void setSeverity(NotificationSeverity severity) { this.severity = severity; }
    public boolean isReadStatus() { return readStatus; }
    public void setReadStatus(boolean readStatus) { this.readStatus = readStatus; }
    public String getRelatedEntityType() { return relatedEntityType; }
    public void setRelatedEntityType(String relatedEntityType) { this.relatedEntityType = relatedEntityType; }
    public Long getRelatedEntityId() { return relatedEntityId; }
    public void setRelatedEntityId(Long relatedEntityId) { this.relatedEntityId = relatedEntityId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
