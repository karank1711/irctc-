package com.irctc.dao;

import com.irctc.model.SearchHit;
import com.irctc.util.DaoSupport;
import com.irctc.util.TextSearch;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class SearchDAO {
    public List<SearchHit> search(String query, int limitPerType) {
        return DaoSupport.query("Search could not be completed.", connection -> {
            String like = TextSearch.contains(query);
            List<SearchHit> hits = new ArrayList<>();
            String bookings = """
                    SELECT b.id, b.pnr, t.train_number, b.source_station, b.destination_station
                    FROM bookings b
                    JOIN trains t ON t.id = b.train_id
                    WHERE b.pnr LIKE ? ESCAPE '\\\\' OR t.train_number LIKE ? ESCAPE '\\\\'
                    ORDER BY b.booking_date DESC
                    LIMIT ?
                    """;
            try (PreparedStatement statement = connection.prepareStatement(bookings)) {
                statement.setString(1, like);
                statement.setString(2, like);
                statement.setInt(3, limitPerType);
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        hits.add(new SearchHit(
                                "Booking",
                                "PNR " + resultSet.getString("pnr"),
                                resultSet.getString("train_number") + " · "
                                        + resultSet.getString("source_station") + " → "
                                        + resultSet.getString("destination_station"),
                                "/bookings/view?id=" + resultSet.getLong("id")));
                    }
                }
            }
            addPeople(connection, hits, """
                    SELECT id, full_name, email FROM users
                    WHERE full_name LIKE ? ESCAPE '\\\\' OR email LIKE ? ESCAPE '\\\\' OR phone LIKE ? ESCAPE '\\\\'
                    ORDER BY full_name LIMIT ?
                    """, "User", "/users/view?id=", like, limitPerType);
            addPeople(connection, hits, """
                    SELECT id, full_name, email FROM employees
                    WHERE full_name LIKE ? ESCAPE '\\\\' OR email LIKE ? ESCAPE '\\\\'
                       OR employee_code LIKE ? ESCAPE '\\\\' OR phone LIKE ? ESCAPE '\\\\'
                    ORDER BY full_name LIMIT ?
                    """, "Employee", "/employees/view?id=", like, limitPerType);
            String trains = """
                    SELECT id, train_number, train_name FROM trains
                    WHERE train_number LIKE ? ESCAPE '\\\\' OR train_name LIKE ? ESCAPE '\\\\'
                    ORDER BY train_number LIMIT ?
                    """;
            try (PreparedStatement statement = connection.prepareStatement(trains)) {
                statement.setString(1, like);
                statement.setString(2, like);
                statement.setInt(3, limitPerType);
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        hits.add(new SearchHit(
                                "Train",
                                resultSet.getString("train_number"),
                                resultSet.getString("train_name"),
                                "/trains/view?id=" + resultSet.getLong("id")));
                    }
                }
            }
            return hits;
        });
    }

    private void addPeople(java.sql.Connection connection, List<SearchHit> hits, String sql, String type, String urlPrefix,
                           String like, int limit) throws java.sql.SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            int index = 1;
            int placeholders = count(sql, '?');
            for (int mark = 0; mark < placeholders - 1; mark++) {
                statement.setString(index++, like);
            }
            statement.setInt(index, limit);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    hits.add(new SearchHit(type, resultSet.getString("full_name"), resultSet.getString("email"),
                            urlPrefix + resultSet.getLong("id")));
                }
            }
        }
    }

    private int count(String sql, char mark) {
        int total = 0;
        for (int index = 0; index < sql.length(); index++) {
            if (sql.charAt(index) == mark) {
                total++;
            }
        }
        return total;
    }
}
