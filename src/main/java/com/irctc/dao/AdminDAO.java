package com.irctc.dao;

import com.irctc.model.Admin;
import com.irctc.util.DaoSupport;
import com.irctc.util.JdbcValues;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class AdminDAO {
    public Admin findByEmail(String email) {
        return DaoSupport.query("The administrator account could not be loaded.", connection -> {
            String sql = "SELECT id, full_name, email, password_hash, active, last_login_at FROM admins WHERE email = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, email);
                try (ResultSet resultSet = statement.executeQuery()) {
                    return resultSet.next() ? map(resultSet) : null;
                }
            }
        });
    }

    public Admin findById(long id) {
        return DaoSupport.query("The administrator account could not be loaded.", connection -> findById(connection, id));
    }

    public Admin findById(Connection connection, long id) throws SQLException {
        String sql = "SELECT id, full_name, email, password_hash, active, last_login_at FROM admins WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? map(resultSet) : null;
            }
        }
    }

    public boolean existsByEmail(String email) {
        return DaoSupport.query("The administrator account could not be checked.", connection -> {
            try (PreparedStatement statement = connection.prepareStatement("SELECT 1 FROM admins WHERE email = ?")) {
                statement.setString(1, email);
                try (ResultSet resultSet = statement.executeQuery()) {
                    return resultSet.next();
                }
            }
        });
    }

    public void insert(Admin admin) {
        DaoSupport.query("The administrator account could not be created.", connection -> {
            String sql = "INSERT INTO admins (full_name, email, password_hash, active) VALUES (?, ?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, admin.getFullName());
                statement.setString(2, admin.getEmail());
                statement.setString(3, admin.getPasswordHash());
                statement.setBoolean(4, admin.isActive());
                statement.executeUpdate();
            }
            return null;
        });
    }

    public void updateLastLogin(long id) {
        DaoSupport.query("The sign-in time could not be saved.", connection -> {
            try (PreparedStatement statement = connection.prepareStatement(
                    "UPDATE admins SET last_login_at = CURRENT_TIMESTAMP WHERE id = ?")) {
                statement.setLong(1, id);
                statement.executeUpdate();
            }
            return null;
        });
    }

    public void updatePassword(Connection connection, long id, String passwordHash) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE admins SET password_hash = ? WHERE id = ?")) {
            statement.setString(1, passwordHash);
            statement.setLong(2, id);
            statement.executeUpdate();
        }
    }

    public void updateName(Connection connection, long id, String fullName) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE admins SET full_name = ? WHERE id = ?")) {
            statement.setString(1, fullName);
            statement.setLong(2, id);
            statement.executeUpdate();
        }
    }

    private Admin map(ResultSet resultSet) throws SQLException {
        Admin admin = new Admin();
        admin.setId(resultSet.getLong("id"));
        admin.setFullName(resultSet.getString("full_name"));
        admin.setEmail(resultSet.getString("email"));
        admin.setPasswordHash(resultSet.getString("password_hash"));
        admin.setActive(resultSet.getBoolean("active"));
        admin.setLastLoginAt(JdbcValues.dateTime(resultSet, "last_login_at"));
        return admin;
    }
}
