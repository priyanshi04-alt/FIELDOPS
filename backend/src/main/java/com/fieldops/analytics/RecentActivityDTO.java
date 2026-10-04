package com.fieldops.analytics;

import java.time.LocalDateTime;

public class RecentActivityDTO {
    private Long id;
    private String title;
    private String description;
    private String type;
    private String timeAgo;
    private LocalDateTime timestamp;
    private String actorName;
    private String actorAvatar;

    public RecentActivityDTO() {}

    public RecentActivityDTO(Long id, String title, String description, String type, String timeAgo,
                             LocalDateTime timestamp, String actorName, String actorAvatar) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.type = type;
        this.timeAgo = timeAgo;
        this.timestamp = timestamp;
        this.actorName = actorName;
        this.actorAvatar = actorAvatar;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private String title;
        private String description;
        private String type;
        private String timeAgo;
        private LocalDateTime timestamp;
        private String actorName;
        private String actorAvatar;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder title(String title) { this.title = title; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder type(String type) { this.type = type; return this; }
        public Builder timeAgo(String timeAgo) { this.timeAgo = timeAgo; return this; }
        public Builder timestamp(LocalDateTime timestamp) { this.timestamp = timestamp; return this; }
        public Builder actorName(String actorName) { this.actorName = actorName; return this; }
        public Builder actorAvatar(String actorAvatar) { this.actorAvatar = actorAvatar; return this; }

        public RecentActivityDTO build() {
            return new RecentActivityDTO(id, title, description, type, timeAgo, timestamp, actorName, actorAvatar);
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getTimeAgo() { return timeAgo; }
    public void setTimeAgo(String timeAgo) { this.timeAgo = timeAgo; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public String getActorName() { return actorName; }
    public void setActorName(String actorName) { this.actorName = actorName; }
    public String getActorAvatar() { return actorAvatar; }
    public void setActorAvatar(String actorAvatar) { this.actorAvatar = actorAvatar; }
}
