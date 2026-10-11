package com.irctc.dao;

import com.irctc.model.RouteSummary;
import com.irctc.model.ServiceType;
import com.irctc.util.DaoSupport;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Queries that feed the dashboard cards and charts. */
public class DashboardDAO {
    public long totalBookings() {
        return count("SELECT COUNT(*) FROM bookings");
    }

    public long activeUsers() {
        return count("SELECT COUNT(*) FROM users WHERE status = 'ACTIVE'");
    }

    public long totalTrains() {
        return count("SELECT COUNT(*) FROM trains");
    }

    public BigDecimal totalRevenue() {
        return money("SELECT COALESCE(SUM(total_amount), 0) FROM bookings WHERE booking_status <> 'CANCELLED'");
    }

    public long bookingsBetween(LocalDateTime start, LocalDateTime end) {
        return countBetween("SELECT COUNT(*) FROM bookings WHERE booking_date >= ? AND booking_date < ?", start, end);
    }

    public BigDecimal revenueBetween(LocalDateTime start, LocalDateTime end) {
        return moneyBetween("""
                SELECT COALESCE(SUM(total_amount), 0) FROM bookings
                WHERE booking_status <> 'CANCELLED' AND booking_date >= ? AND booking_date < ?
                """, start, end);
    }

    public long usersCreatedBetween(LocalDateTime start, LocalDateTime end) {
        return countBetween("SELECT COUNT(*) FROM users WHERE created_at >= ? AND created_at < ?", start, end);
    }

    public long trainsCreatedBetween(LocalDateTime start, LocalDateTime end) {
        return countBetween("SELECT COUNT(*) FROM trains WHERE created_at >= ? AND created_at < ?", start, end);
    }

    public Map<LocalDate, Long> dailyBookings(LocalDateTime start, LocalDateTime end) {
        return dailyCount("""
                SELECT DATE(booking_date) AS day_value, COUNT(*) AS total
                FROM bookings
                WHERE booking_date >= ? AND booking_date < ?
                GROUP BY DATE(booking_date)
                ORDER BY day_value
                """, start, end);
    }

    public Map<LocalDate, Long> dailyUsers(LocalDateTime start, LocalDateTime end) {
        return dailyCount("""
                SELECT DATE(created_at) AS day_value, COUNT(*) AS total
                FROM users
                WHERE created_at >= ? AND created_at < ?
                GROUP BY DATE(created_at)
                """, start, end);
    }

    public Map<LocalDate, Long> dailyTrains(LocalDateTime start, LocalDateTime end) {
        return dailyCount("""
                SELECT DATE(created_at) AS day_value, COUNT(*) AS total
                FROM trains
                WHERE created_at >= ? AND created_at < ?
                GROUP BY DATE(created_at)
                """, start, end);
    }

    public Map<LocalDate, Long> dailyRevenue(LocalDateTime start, LocalDateTime end) {
        return DaoSupport.query("Revenue history could not be loaded.", connection -> {
            Map<LocalDate, Long> values = new LinkedHashMap<>();
            String sql = """
                    SELECT DATE(booking_date) AS day_value,
                           COALESCE(SUM(CASE WHEN booking_status <> 'CANCELLED' THEN total_amount ELSE 0 END), 0) AS total
                    FROM bookings
                    WHERE booking_date >= ? AND booking_date < ?
                    GROUP BY DATE(booking_date)
                    """;
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setTimestamp(1, Timestamp.valueOf(start));
                statement.setTimestamp(2, Timestamp.valueOf(end));
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        BigDecimal total = resultSet.getBigDecimal("total");
                        values.put(resultSet.getDate("day_value").toLocalDate(), total.longValue());
                    }
                }
            }
            return values;
        });
    }

    public List<ServiceType> serviceBreakdown() {
        return DaoSupport.query("Service totals could not be loaded.", connection -> {
            String sql = """
                    SELECT s.code, s.name, s.description, s.offered, s.sort_order, COUNT(b.id) AS total
                    FROM service_types s
                    LEFT JOIN bookings b ON b.service_type = s.code
                    GROUP BY s.code, s.name, s.description, s.offered, s.sort_order
                    ORDER BY s.sort_order
                    """;
            List<ServiceType> services = new ArrayList<>();
            try (PreparedStatement statement = connection.prepareStatement(sql);
                 ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    ServiceType service = new ServiceType();
                    service.setCode(resultSet.getString("code"));
                    service.setName(resultSet.getString("name"));
                    service.setDescription(resultSet.getString("description"));
                    service.setOffered(resultSet.getBoolean("offered"));
                    service.setSortOrder(resultSet.getInt("sort_order"));
                    service.setBookingCount(resultSet.getLong("total"));
                    services.add(service);
                }
            }
            return services;
        });
    }

    public List<ServiceType> classBreakdown() {
        return DaoSupport.query("Class totals could not be loaded.", connection -> {
            String sql = """
                    SELECT c.code, c.display_name, c.color_hex, c.visible, c.sort_order, COUNT(b.id) AS total
                    FROM booking_classes c
                    LEFT JOIN bookings b ON b.travel_class = c.code
                    GROUP BY c.code, c.display_name, c.color_hex, c.visible, c.sort_order
                    ORDER BY c.sort_order, c.code
                    """;
            List<ServiceType> classes = new ArrayList<>();
            try (PreparedStatement statement = connection.prepareStatement(sql);
                 ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    ServiceType travelClass = new ServiceType();
                    travelClass.setCode(resultSet.getString("code"));
                    travelClass.setName(resultSet.getString("display_name"));
                    travelClass.setColor(resultSet.getString("color_hex"));
                    travelClass.setOffered(resultSet.getBoolean("visible"));
                    travelClass.setSortOrder(resultSet.getInt("sort_order"));
                    travelClass.setBookingCount(resultSet.getLong("total"));
                    classes.add(travelClass);
                }
            }
            return classes;
        });
    }

    public List<RouteSummary> topRoutes(int limit) {
        return DaoSupport.query("Routes could not be loaded.", connection -> {
            String sql = """
                    SELECT b.source_station, b.destination_station,
                           COALESCE(src.city, b.source_station) AS source_city,
                           COALESCE(dst.city, b.destination_station) AS destination_city,
                           COUNT(*) AS total
                    FROM bookings b
                    LEFT JOIN stations src ON src.station_code = b.source_station
                    LEFT JOIN stations dst ON dst.station_code = b.destination_station
                    WHERE b.booking_status <> 'CANCELLED'
                    GROUP BY b.source_station, b.destination_station, src.city, dst.city
                    ORDER BY total DESC, source_city
                    LIMIT ?
                    """;
            List<RouteSummary> routes = new ArrayList<>();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, limit);
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        RouteSummary route = new RouteSummary();
                        route.setSource(resultSet.getString("source_station"));
                        route.setDestination(resultSet.getString("destination_station"));
                        route.setSourceCity(resultSet.getString("source_city"));
                        route.setDestinationCity(resultSet.getString("destination_city"));
                        route.setBookings(resultSet.getLong("total"));
                        routes.add(route);
                    }
                }
            }
            return routes;
        });
    }

    private long count(String sql) {
        return DaoSupport.query("A dashboard total could not be loaded.", connection -> {
            try (PreparedStatement statement = connection.prepareStatement(sql);
                 ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? resultSet.getLong(1) : 0L;
            }
        });
    }

    private long countBetween(String sql, LocalDateTime start, LocalDateTime end) {
        return DaoSupport.query("A dashboard comparison could not be loaded.", connection -> {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setTimestamp(1, Timestamp.valueOf(start));
                statement.setTimestamp(2, Timestamp.valueOf(end));
                try (ResultSet resultSet = statement.executeQuery()) {
                    return resultSet.next() ? resultSet.getLong(1) : 0L;
                }
            }
        });
    }

    private BigDecimal money(String sql) {
        return DaoSupport.query("Revenue could not be loaded.", connection -> {
            try (PreparedStatement statement = connection.prepareStatement(sql);
                 ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? resultSet.getBigDecimal(1) : BigDecimal.ZERO;
            }
        });
    }

    private BigDecimal moneyBetween(String sql, LocalDateTime start, LocalDateTime end) {
        return DaoSupport.query("Revenue could not be loaded.", connection -> {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setTimestamp(1, Timestamp.valueOf(start));
                statement.setTimestamp(2, Timestamp.valueOf(end));
                try (ResultSet resultSet = statement.executeQuery()) {
                    return resultSet.next() ? resultSet.getBigDecimal(1) : BigDecimal.ZERO;
                }
            }
        });
    }

    private Map<LocalDate, Long> dailyCount(String sql, LocalDateTime start, LocalDateTime end) {
        return DaoSupport.query("Chart data could not be loaded.", connection -> {
            Map<LocalDate, Long> values = new LinkedHashMap<>();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setTimestamp(1, Timestamp.valueOf(start));
                statement.setTimestamp(2, Timestamp.valueOf(end));
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        values.put(resultSet.getDate("day_value").toLocalDate(), resultSet.getLong("total"));
                    }
                }
            }
            return values;
        });
    }
}
