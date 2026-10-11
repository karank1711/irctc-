package com.irctc.dao;

import com.irctc.util.DaoSupport;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.LinkedHashMap;
import java.util.Map;

public class SettingsDAO {
    public Map<String, String> findAll() {
        return DaoSupport.query("Settings could not be loaded.", connection -> {
            Map<String, String> settings = new LinkedHashMap<>();
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT setting_key, setting_value FROM app_settings ORDER BY setting_key");
                 ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    settings.put(resultSet.getString("setting_key"), resultSet.getString("setting_value"));
                }
            }
            return settings;
        });
    }

    public void upsert(Connection connection, String key, String value) throws java.sql.SQLException {
        String sql = """
                INSERT INTO app_settings (setting_key, setting_value) VALUES (?, ?)
                ON DUPLICATE KEY UPDATE setting_value = VALUES(setting_value)
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, key);
            statement.setString(2, value);
            statement.executeUpdate();
        }
    }
}
