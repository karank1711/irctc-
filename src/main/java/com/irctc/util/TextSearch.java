package com.irctc.util;

/** Prepares a user search string for a SQL LIKE clause. */
public final class TextSearch {
    private TextSearch() {
    }

    public static String contains(String raw) {
        String value = raw == null ? "" : raw.trim();
        String escaped = value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
        return "%" + escaped + "%";
    }
}
