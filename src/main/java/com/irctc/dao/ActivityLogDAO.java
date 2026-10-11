package com.irctc.dao;

import com.irctc.model.ActivityLog;
import com.irctc.model.PageResult;
import com.irctc.util.DaoSupport;
import com.irctc.util.JdbcValues;
import com.irctc.util.Pagination;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ActivityLogDAO {
    private static final String COLUMNS = "id, admin_id, activity_type, message, entity_type, entity_id, read_at, created_at";

    public void insert(Connection connection, Long adminId, String type, String message, String entityType, Long entityId)
            throws SQLException {
        String sql = """
                INSERT INTO activity_logs (admin_id, activity_type, message, entity_type, entity_id)
                VALUES (?, ?, ?, ?, ?)
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            if (adminId == null) {
                statement.setNull(1, java.sql.Types.BIGINT);
            } else {
                statement.setLong(1, adminId);
            }
            statement.setString(2, type);
            statement.setString(3, message);
            statement.setString(4, entityType);
            if (entityId == null) {
                statement.setNull(5, java.sql.Types.BIGINT);
            } else {
                statement.setLong(5, entityId);
            }
            statement.executeUpdate();
        }
    }

    public List<ActivityLog> latest(int limit) {
        return DaoSupport.query("Activities could not be loaded.", connection -> {
            String sql = "SELECT " + COLUMNS + " FROM activity_logs ORDER BY created_at DESC, id DESC LIMIT ?";
            return list(connection, sql, List.of(limit));
        });
    }

    public int countUnread() {
        return DaoSupport.query("Notifications could not be loaded.", connection -> {
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT COUNT(*) FROM activity_logs WHERE read_at IS NULL")) {
                try (ResultSet resultSet = statement.executeQuery()) {
                    return resultSet.next() ? resultSet.getInt(1) : 0;
                }
            }
        });
    }

    public PageResult<ActivityLog> search(String type, int page, int pageSize) {
        return DaoSupport.query("Activities could not be loaded.", connection -> {
            String where = " WHERE (? = '' OR activity_type = ?)";
            String safeType = type == null ? "" : type;
            List<Object> filters = List.of(safeType, safeType);
            int total;
            try (PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM activity_logs" + where)) {
                DaoSupport.bind(statement, filters);
                try (ResultSet resultSet = statement.executeQuery()) {
                    total = resultSet.next() ? resultSet.getInt(1) : 0;
                }
            }
            int totalPages = Pagination.totalPages(total, pageSize);
            int safePage = Pagination.clamp(page, totalPages);
            List<Object> params = new ArrayList<>(filters);
            params.add(pageSize);
            params.add(Pagination.offset(safePage, pageSize));
            String sql = "SELECT " + COLUMNS + " FROM activity_logs" + where + " ORDER BY created_at DESC, id DESC LIMIT ? OFFSET ?";
            return new PageResult<>(list(connection, sql, params), safePage, pageSize, total, totalPages);
        });
    }

    public void markAllRead(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("UPDATE activity_logs SET read_at = CURRENT_TIMESTAMP WHERE read_at IS NULL");
        }
    }

    private List<ActivityLog> list(Connection connection, String sql, List<Object> params) throws SQLException {
        List<ActivityLog> logs = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            DaoSupport.bind(statement, params);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    logs.add(map(resultSet));
                }
            }
        }
        return logs;
    }

    private ActivityLog map(ResultSet resultSet) throws SQLException {
        ActivityLog log = new ActivityLog();
        log.setId(resultSet.getLong("id"));
        long adminId = resultSet.getLong("admin_id");
        log.setAdminId(resultSet.wasNull() ? null : adminId);
        log.setActivityType(resultSet.getString("activity_type"));
        log.setMessage(resultSet.getString("message"));
        log.setEntityType(resultSet.getString("entity_type"));
        long entityId = resultSet.getLong("entity_id");
        log.setEntityId(resultSet.wasNull() ? null : entityId);
        log.setCreatedAt(JdbcValues.dateTime(resultSet, "created_at"));
        resultSet.getTimestamp("read_at");
        log.setUnread(resultSet.wasNull());
        return log;
    }
}
