package com.irctc.util;

/** Writes one safe CSV cell. */
public final class Csv {
    private Csv() {
    }

    public static String cell(String value) {
        if (value == null) {
            return "";
        }
        boolean mustQuote = value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r");
        String escaped = value.replace("\"", "\"\"");
        return mustQuote ? "\"" + escaped + "\"" : escaped;
    }

    public static String row(String... values) {
        StringBuilder builder = new StringBuilder();
        for (int index = 0; index < values.length; index++) {
            if (index > 0) {
                builder.append(',');
            }
            builder.append(cell(values[index]));
        }
        return builder.append("\r\n").toString();
    }
}
