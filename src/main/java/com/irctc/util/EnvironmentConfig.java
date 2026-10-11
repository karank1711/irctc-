package com.irctc.util;

import io.github.cdimascio.dotenv.Dotenv;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Loads configuration from the operating system environment and from a .env file.
 * Java does not load .env files by itself, so this class uses the dotenv library.
 * System environment variables win over the file, which is useful in production.
 */
public final class EnvironmentConfig {
    private static final EnvironmentConfig INSTANCE = new EnvironmentConfig();
    private final Map<String, String> values;

    private EnvironmentConfig() {
        Map<String, String> loaded = new HashMap<>();
        File envFile = findEnvFile(new File(System.getProperty("user.dir")));
        Dotenv dotenv = Dotenv.configure()
                .directory(envFile.getParentFile().getAbsolutePath())
                .filename(envFile.getName())
                .ignoreIfMissing()
                .ignoreIfMalformed()
                .load();
        dotenv.entries().forEach(entry -> loaded.put(entry.getKey(), entry.getValue()));
        System.getenv().forEach(loaded::put);
        this.values = Map.copyOf(loaded);
    }

    public static EnvironmentConfig getInstance() {
        return INSTANCE;
    }

    public String get(String key) {
        String value = values.get(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing environment variable: " + key);
        }
        return value.trim();
    }

    /** Password fields may be blank only when the key itself is present. */
    public String getAllowEmpty(String key) {
        if (!values.containsKey(key) || values.get(key) == null) {
            throw new IllegalStateException("Missing environment variable: " + key);
        }
        return values.get(key).trim();
    }

    public String getOrDefault(String key, String fallback) {
        String value = values.get(key);
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value.trim();
    }

    private static File findEnvFile(File start) {
        File current = start;
        while (current != null) {
            File candidate = new File(current, ".env");
            if (candidate.isFile()) {
                return candidate;
            }
            current = current.getParentFile();
        }
        return new File(start, ".env");
    }
}
