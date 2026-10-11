package com.irctc.util;

import java.util.ArrayList;
import java.util.List;

/** Splits a .sql file into statements without breaking quoted text. */
public final class SqlScript {
    private SqlScript() {
    }

    public static List<String> statements(String script) {
        List<String> statements = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inSingleQuote = false;
        boolean inDoubleQuote = false;
        for (int index = 0; index < script.length(); index++) {
            char currentChar = script.charAt(index);
            char next = index + 1 < script.length() ? script.charAt(index + 1) : '\0';
            if (!inSingleQuote && !inDoubleQuote && currentChar == '-' && next == '-') {
                while (index < script.length() && script.charAt(index) != '\n') {
                    index++;
                }
                current.append('\n');
                continue;
            }
            if (!inDoubleQuote && currentChar == '\'') {
                if (inSingleQuote && next == '\'') {
                    current.append(currentChar).append(next);
                    index++;
                    continue;
                }
                inSingleQuote = !inSingleQuote;
                current.append(currentChar);
                continue;
            }
            if (!inSingleQuote && currentChar == '"') {
                inDoubleQuote = !inDoubleQuote;
                current.append(currentChar);
                continue;
            }
            if (!inSingleQuote && !inDoubleQuote && currentChar == ';') {
                addIfPresent(statements, current);
                continue;
            }
            current.append(currentChar);
        }
        addIfPresent(statements, current);
        return statements;
    }

    private static void addIfPresent(List<String> statements, StringBuilder current) {
        String sql = current.toString().trim();
        current.setLength(0);
        if (!sql.isEmpty()) {
            statements.add(sql);
        }
    }
}
