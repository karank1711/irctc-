package com.irctc.util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/** Reads date columns from a JDBC result without repeating null checks. */
public final class JdbcValues {
    private JdbcValues() {
    }

    public static LocalDateTime dateTime(ResultSet resultSet, String column) throws SQLException {
        Timestamp timestamp = resultSet.getTimestamp(column);
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }

    public static LocalDate date(ResultSet resultSet, String column) throws SQLException {
        java.sql.Date date = resultSet.getDate(column);
        return date == null ? null : date.toLocalDate();
    }

    public static LocalTime time(ResultSet resultSet, String column) throws SQLException {
        java.sql.Time time = resultSet.getTime(column);
        return time == null ? null : time.toLocalTime();
    }
}
