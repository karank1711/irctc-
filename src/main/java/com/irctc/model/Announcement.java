package com.irctc.model;

import com.irctc.util.Formatters;

import java.time.LocalDateTime;

/** A short notice an administrator can show above the dashboard. */
public class Announcement {
    private long id;
    private String title;
    private String message;
    private boolean active;
    private LocalDateTime createdAt;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getCreatedLabel() {
        return Formatters.dateTime(createdAt);
    }
}
