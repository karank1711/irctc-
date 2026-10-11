package com.irctc.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.UUID;

/** Stores uploaded banner files outside the web root and serves them by file name only. */
public final class UploadStore {
    private UploadStore() {
    }

    public static Path directory() throws IOException {
        Path directory = Path.of(System.getProperty("user.dir"), "data", "uploads").toAbsolutePath().normalize();
        Files.createDirectories(directory);
        return directory;
    }

    public static Path resolve(String fileName) throws IOException {
        if (fileName == null || !fileName.matches("[A-Za-z0-9._-]+")) {
            return null;
        }
        Path directory = directory();
        Path file = directory.resolve(fileName).normalize();
        if (!file.startsWith(directory)) {
            return null;
        }
        return file;
    }

    public static String save(InputStream input, String contentType) throws IOException {
        String extension = switch (contentType == null ? "" : contentType.toLowerCase(Locale.ROOT)) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> "";
        };
        if (extension.isEmpty()) {
            throw new IOException("Unsupported image type");
        }
        String fileName = "banner-" + UUID.randomUUID() + extension;
        Path target = directory().resolve(fileName);
        Files.copy(input, target);
        return fileName;
    }
}
