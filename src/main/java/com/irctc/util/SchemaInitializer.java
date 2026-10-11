package com.irctc.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Creates tables on startup. Business rows are not invented here.
 */
public final class SchemaInitializer {
    private static final Logger LOGGER = Logger.getLogger(SchemaInitializer.class.getName());

    public void initialize() {
        EnvironmentConfig config = EnvironmentConfig.getInstance();
        if (!Boolean.parseBoolean(config.getOrDefault("DB_AUTO_INIT", "true"))) {
            return;
        }
        run("schema.sql", false);
    }

    private void run(String fileName, boolean useTransaction) {
        String script = read(fileName);
        List<String> statements = SqlScript.statements(script);
        String message = "The database script " + fileName + " could not be applied.";
        if (!useTransaction) {
            DaoSupport.query(message, connection -> executeAll(connection, statements));
            return;
        }
        DaoSupport.transaction(message, connection -> {
            executeAll(connection, statements);
        });
    }

    private static Void executeAll(Connection connection, List<String> statements) throws SQLException {
        for (String sql : statements) {
            try (Statement statement = connection.createStatement()) {
                statement.execute(sql);
            }
        }
        return null;
    }

    private String read(String fileName) {
        String path = "/sql/" + fileName;
        try (InputStream input = SchemaInitializer.class.getResourceAsStream(path)) {
            if (input == null) {
                throw new IllegalStateException("SQL script is missing from the classpath: " + fileName);
            }
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Could not read " + fileName, ex);
            throw new IllegalStateException("SQL script could not be read: " + fileName, ex);
        }
    }
}
