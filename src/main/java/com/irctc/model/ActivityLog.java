package com.irctc.model;

import com.irctc.util.Formatters;

import java.time.LocalDateTime;

public class ActivityLog {
    private long id;
    private Long adminId;
    private String activityType;
    private String message;
    private String entityType;
    private Long entityId;
    private LocalDateTime createdAt;
    private boolean unread;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Long getAdminId() {
        return adminId;
    }

    public void setAdminId(Long adminId) {
        this.adminId = adminId;
    }

    public String getActivityType() {
        return activityType;
    }

    public void setActivityType(String activityType) {
        this.activityType = activityType;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getTimeLabel() {
        return Formatters.relative(createdAt);
    }

    public String getExactTimeLabel() {
        return Formatters.dateTime(createdAt);
    }

    public boolean isUnread() {
        return unread;
    }

    public void setUnread(boolean unread) {
        this.unread = unread;
    }

    public String getIcon() {
        if (activityType == null) {
            return "activity";
        }
        return switch (activityType) {
            case "BOOKING_CREATED", "BOOKING_CANCELLED" -> "ticket";
            case "USER_REGISTERED", "USER_UPDATED" -> "users";
            case "EMPLOYEE_ADDED", "EMPLOYEE_UPDATED" -> "id-card";
            case "TRAIN_UPDATED", "SCHEDULE_UPDATED" -> "train-front";
            case "REPORT_GENERATED" -> "file-bar-chart";
            case "SETTINGS_UPDATED" -> "settings";
            case "ADMIN_LOGIN" -> "log-in";
            default -> "activity";
        };
    }

    public String getTypeLabel() {
        if (activityType == null) {
            return "";
        }
        return switch (activityType) {
            case "BOOKING_CREATED" -> "Booking created";
            case "BOOKING_CANCELLED" -> "Booking cancelled";
            case "USER_REGISTERED" -> "User registered";
            case "USER_UPDATED" -> "User updated";
            case "EMPLOYEE_ADDED" -> "Employee added";
            case "EMPLOYEE_UPDATED" -> "Employee updated";
            case "TRAIN_UPDATED" -> "Train updated";
            case "SCHEDULE_UPDATED" -> "Schedule updated";
            case "REPORT_GENERATED" -> "Report generated";
            case "SETTINGS_UPDATED" -> "Settings updated";
            case "ADMIN_LOGIN" -> "Administrator signed in";
            case "CONTENT_UPDATED" -> "Content updated";
            case "PROFILE_UPDATED" -> "Profile updated";
            default -> activityType;
        };
    }
}
