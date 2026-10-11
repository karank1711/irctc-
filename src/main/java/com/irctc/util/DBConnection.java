package com.irctc.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** Opens a JDBC connection using the values in the .env file. */
public final class DBConnection {
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException ex) {
            throw new IllegalStateException("MySQL JDBC driver is not on the classpath.", ex);
        }
    }

    private DBConnection() {
    }

    public static Connection getConnection() throws SQLException {
        EnvironmentConfig config = EnvironmentConfig.getInstance();
        return DriverManager.getConnection(
                config.get("DB_URL"),
                config.get("DB_USERNAME"),
                config.getAllowEmpty("DB_PASSWORD"));
    }
}
