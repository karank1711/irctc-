package com.irctc.service;

import com.irctc.dao.ActivityLogDAO;
import com.irctc.util.DaoSupport;

import java.sql.Connection;
import java.sql.SQLException;

public class ActivityService {
    private final ActivityLogDAO activityLogDAO = new ActivityLogDAO();

    public void record(Connection connection, long adminId, String type, String message, String entityType, Long entityId)
            throws SQLException {
        activityLogDAO.insert(connection, adminId, type, limit(message), entityType, entityId);
    }

    public void record(long adminId, String type, String message, String entityType, Long entityId) {
        DaoSupport.transaction("The activity could not be saved.", connection ->
                record(connection, adminId, type, message, entityType, entityId));
    }

    public void markAllRead() {
        DaoSupport.transaction("Notifications could not be updated.", activityLogDAO::markAllRead);
    }

    private String limit(String message) {
        if (message == null) {
            return "";
        }
        return message.length() <= 255 ? message : message.substring(0, 252) + "...";
    }
}
