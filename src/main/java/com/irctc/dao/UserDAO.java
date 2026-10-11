package com.irctc.dao;

import com.irctc.model.PageResult;
import com.irctc.model.User;
import com.irctc.util.DaoSupport;
import com.irctc.util.JdbcValues;
import com.irctc.util.Pagination;
import com.irctc.util.TextSearch;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    private static final String COLUMNS = "id, full_name, email, phone, status, created_at, updated_at";

    public PageResult<User> search(String query, String status, int page, int pageSize) {
        return DaoSupport.query("Users could not be loaded.", connection -> {
            String where = " WHERE (? = '' OR full_name LIKE ? ESCAPE '\\\\' OR email LIKE ? ESCAPE '\\\\' OR phone LIKE ? ESCAPE '\\\\')"
                    + " AND (? = '' OR status = ?)";
            List<Object> filters = filters(query, status);
            int total = scalar(connection, "SELECT COUNT(*) FROM users" + where, filters);
            int totalPages = Pagination.totalPages(total, pageSize);
            int safePage = Pagination.clamp(page, totalPages);
            List<Object> params = new ArrayList<>(filters);
            params.add(pageSize);
            params.add(Pagination.offset(safePage, pageSize));
            List<User> items = new ArrayList<>();
            String sql = "SELECT " + COLUMNS + " FROM users" + where + " ORDER BY created_at DESC, id DESC LIMIT ? OFFSET ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                DaoSupport.bind(statement, params);
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        items.add(map(resultSet));
                    }
                }
            }
            return new PageResult<>(items, safePage, pageSize, total, totalPages);
        });
    }

    public User findById(long id) {
        return DaoSupport.query("The user could not be loaded.", connection -> findById(connection, id));
    }

    public User findById(Connection connection, long id) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("SELECT " + COLUMNS + " FROM users WHERE id = ?")) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? map(resultSet) : null;
            }
        }
    }

    public boolean emailTaken(Connection connection, String email, long exceptId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT 1 FROM users WHERE email = ? AND id <> ?")) {
            statement.setString(1, email);
            statement.setLong(2, exceptId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    public long insert(Connection connection, User user) throws SQLException {
        String sql = "INSERT INTO users (full_name, email, phone, status) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, user.getFullName());
            statement.setString(2, user.getEmail());
            statement.setString(3, user.getPhone());
            statement.setString(4, user.getStatus());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
        }
        throw new SQLException("The user could not be saved.");
    }

    public void update(Connection connection, User user) throws SQLException {
        String sql = "UPDATE users SET full_name = ?, email = ?, phone = ?, status = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, user.getFullName());
            statement.setString(2, user.getEmail());
            statement.setString(3, user.getPhone());
            statement.setString(4, user.getStatus());
            statement.setLong(5, user.getId());
            statement.executeUpdate();
        }
    }

    public void updateStatus(Connection connection, long id, String status) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("UPDATE users SET status = ? WHERE id = ?")) {
            statement.setString(1, status);
            statement.setLong(2, id);
            statement.executeUpdate();
        }
    }

    public List<User> activeUsers() {
        return DaoSupport.query("Users could not be loaded.", connection -> {
            List<User> users = new ArrayList<>();
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT " + COLUMNS + " FROM users WHERE status = 'ACTIVE' ORDER BY full_name")) {
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        users.add(map(resultSet));
                    }
                }
            }
            return users;
        });
    }

    private List<Object> filters(String query, String status) {
        String like = query == null || query.isBlank() ? "%" : TextSearch.contains(query);
        String safeQuery = query == null ? "" : query.trim();
        String safeStatus = status == null ? "" : status.trim();
        return List.of(safeQuery, like, like, like, safeStatus, safeStatus);
    }

    private int scalar(Connection connection, String sql, List<Object> params) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            DaoSupport.bind(statement, params);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? resultSet.getInt(1) : 0;
            }
        }
    }

    private User map(ResultSet resultSet) throws SQLException {
        User user = new User();
        user.setId(resultSet.getLong("id"));
        user.setFullName(resultSet.getString("full_name"));
        user.setEmail(resultSet.getString("email"));
        user.setPhone(resultSet.getString("phone"));
        user.setStatus(resultSet.getString("status"));
        user.setCreatedAt(JdbcValues.dateTime(resultSet, "created_at"));
        user.setUpdatedAt(JdbcValues.dateTime(resultSet, "updated_at"));
        return user;
    }
}
