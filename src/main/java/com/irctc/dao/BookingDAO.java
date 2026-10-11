package com.irctc.dao;

import com.irctc.model.Booking;
import com.irctc.model.PageResult;
import com.irctc.model.ReportSummary;
import com.irctc.model.RouteSummary;
import com.irctc.util.DaoSupport;
import com.irctc.util.JdbcValues;
import com.irctc.util.Pagination;
import com.irctc.util.TextSearch;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BookingDAO {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String SELECT = """
            SELECT b.id, b.pnr, b.user_id, u.full_name AS passenger_name, b.train_id, t.train_number, t.train_name,
                   b.source_station, b.destination_station, b.travel_class, b.service_type, s.name AS service_name,
                   b.booking_date, b.travel_date, b.booking_status, b.total_amount
            FROM bookings b
            JOIN users u ON u.id = b.user_id
            JOIN trains t ON t.id = b.train_id
            JOIN service_types s ON s.code = b.service_type
            """;

    public PageResult<Booking> search(String query, String status, int page, int pageSize) {
        return DaoSupport.query("Bookings could not be loaded.", connection -> {
            Filter filter = filter(query, status, null, null);
            int total = count(connection, filter);
            int totalPages = Pagination.totalPages(total, pageSize);
            int safePage = Pagination.clamp(page, totalPages);
            List<Object> params = new ArrayList<>(filter.params);
            params.add(pageSize);
            params.add(Pagination.offset(safePage, pageSize));
            String sql = SELECT + filter.where + " ORDER BY b.booking_date DESC, b.id DESC LIMIT ? OFFSET ?";
            return new PageResult<>(list(connection, sql, params), safePage, pageSize, total, totalPages);
        });
    }

    public List<Booking> recent(int limit) {
        return DaoSupport.query("Recent bookings could not be loaded.", connection ->
                list(connection, SELECT + " ORDER BY b.booking_date DESC, b.id DESC LIMIT ?", List.of(limit)));
    }

    public Booking findById(long id) {
        return DaoSupport.query("The booking could not be loaded.", connection -> {
            List<Booking> bookings = list(connection, SELECT + " WHERE b.id = ?", List.of(id));
            return bookings.isEmpty() ? null : bookings.get(0);
        });
    }

    public ReportSummary report(LocalDateTime start, LocalDateTime end, int rowLimit) {
        return DaoSupport.query("The report could not be loaded.", connection -> {
            ReportSummary summary = new ReportSummary();
            String sql = """
                    SELECT COUNT(*) AS total_bookings,
                           SUM(CASE WHEN booking_status = 'CONFIRMED' THEN 1 ELSE 0 END) AS confirmed_count,
                           SUM(CASE WHEN booking_status = 'WAITING' THEN 1 ELSE 0 END) AS waiting_count,
                           SUM(CASE WHEN booking_status = 'RAC' THEN 1 ELSE 0 END) AS rac_count,
                           SUM(CASE WHEN booking_status = 'CANCELLED' THEN 1 ELSE 0 END) AS cancelled_count,
                           COALESCE(SUM(CASE WHEN booking_status <> 'CANCELLED' THEN total_amount ELSE 0 END), 0) AS revenue
                    FROM bookings
                    WHERE booking_date >= ? AND booking_date < ?
                    """;
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setTimestamp(1, Timestamp.valueOf(start));
                statement.setTimestamp(2, Timestamp.valueOf(end));
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        summary.setTotalBookings(resultSet.getInt("total_bookings"));
                        summary.setConfirmed(resultSet.getInt("confirmed_count"));
                        summary.setWaiting(resultSet.getInt("waiting_count"));
                        summary.setRac(resultSet.getInt("rac_count"));
                        summary.setCancelled(resultSet.getInt("cancelled_count"));
                        summary.setRevenue(resultSet.getBigDecimal("revenue"));
                    }
                }
            }
            summary.getRoutes().addAll(routes(connection, start, end, 20));
            Filter filter = filter("", "", start, end);
            List<Object> params = new ArrayList<>(filter.params);
            params.add(rowLimit);
            summary.getBookings().addAll(list(connection,
                    SELECT + filter.where + " ORDER BY b.booking_date DESC, b.id DESC LIMIT ?", params));
            return summary;
        });
    }

    public String nextPnr(Connection connection) throws SQLException {
        for (int attempt = 0; attempt < 8; attempt++) {
            String pnr = randomPnr();
            try (PreparedStatement statement = connection.prepareStatement("SELECT 1 FROM bookings WHERE pnr = ?")) {
                statement.setString(1, pnr);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (!resultSet.next()) {
                        return pnr;
                    }
                }
            }
        }
        throw new SQLException("A unique PNR could not be created.");
    }

    public long insert(Connection connection, Booking booking) throws SQLException {
        String sql = """
                INSERT INTO bookings (pnr, user_id, train_id, source_station, destination_station, travel_class,
                    service_type, booking_date, travel_date, booking_status, total_amount)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, booking.getPnr());
            statement.setLong(2, booking.getUserId());
            statement.setLong(3, booking.getTrainId());
            statement.setString(4, booking.getSourceStation());
            statement.setString(5, booking.getDestinationStation());
            statement.setString(6, booking.getTravelClass());
            statement.setString(7, booking.getServiceType());
            statement.setTimestamp(8, Timestamp.valueOf(booking.getBookingDate()));
            statement.setDate(9, java.sql.Date.valueOf(booking.getTravelDate()));
            statement.setString(10, booking.getBookingStatus());
            statement.setBigDecimal(11, booking.getTotalAmount());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
        }
        throw new SQLException("The booking could not be saved.");
    }

    public boolean cancel(Connection connection, long id) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE bookings SET booking_status = 'CANCELLED' WHERE id = ? AND booking_status <> 'CANCELLED'")) {
            statement.setLong(1, id);
            return statement.executeUpdate() > 0;
        }
    }

    private List<RouteSummary> routes(Connection connection, LocalDateTime start, LocalDateTime end, int limit)
            throws SQLException {
        String sql = """
                SELECT source_station, destination_station, COUNT(*) AS total
                FROM bookings
                WHERE booking_status <> 'CANCELLED' AND booking_date >= ? AND booking_date < ?
                GROUP BY source_station, destination_station
                ORDER BY total DESC, source_station
                LIMIT ?
                """;
        List<RouteSummary> routes = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setTimestamp(1, Timestamp.valueOf(start));
            statement.setTimestamp(2, Timestamp.valueOf(end));
            statement.setInt(3, limit);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    routes.add(mapRoute(resultSet));
                }
            }
        }
        return routes;
    }

    private Filter filter(String query, String status, LocalDateTime start, LocalDateTime end) {
        StringBuilder where = new StringBuilder(" WHERE 1 = 1");
        List<Object> params = new ArrayList<>();
        if (query != null && !query.isBlank()) {
            where.append(" AND (b.pnr LIKE ? ESCAPE '\\\\' OR t.train_number LIKE ? ESCAPE '\\\\' OR u.full_name LIKE ? ESCAPE '\\\\')");
            String like = TextSearch.contains(query);
            params.add(like);
            params.add(like);
            params.add(like);
        }
        if (status != null && !status.isBlank()) {
            where.append(" AND b.booking_status = ?");
            params.add(status);
        }
        if (start != null && end != null) {
            where.append(" AND b.booking_date >= ? AND b.booking_date < ?");
            params.add(start);
            params.add(end);
        }
        return new Filter(where.toString(), params);
    }

    private int count(Connection connection, Filter filter) throws SQLException {
        String sql = """
                SELECT COUNT(*)
                FROM bookings b
                JOIN users u ON u.id = b.user_id
                JOIN trains t ON t.id = b.train_id
                """ + filter.where;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            DaoSupport.bind(statement, filter.params);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? resultSet.getInt(1) : 0;
            }
        }
    }

    private List<Booking> list(Connection connection, String sql, List<Object> params) throws SQLException {
        List<Booking> bookings = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            DaoSupport.bind(statement, params);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    bookings.add(map(resultSet));
                }
            }
        }
        return bookings;
    }

    private Booking map(ResultSet resultSet) throws SQLException {
        Booking booking = new Booking();
        booking.setId(resultSet.getLong("id"));
        booking.setPnr(resultSet.getString("pnr"));
        booking.setUserId(resultSet.getLong("user_id"));
        booking.setPassengerName(resultSet.getString("passenger_name"));
        booking.setTrainId(resultSet.getLong("train_id"));
        booking.setTrainNumber(resultSet.getString("train_number"));
        booking.setTrainName(resultSet.getString("train_name"));
        booking.setSourceStation(resultSet.getString("source_station"));
        booking.setDestinationStation(resultSet.getString("destination_station"));
        booking.setTravelClass(resultSet.getString("travel_class"));
        booking.setServiceType(resultSet.getString("service_type"));
        booking.setServiceName(resultSet.getString("service_name"));
        booking.setBookingDate(JdbcValues.dateTime(resultSet, "booking_date"));
        booking.setTravelDate(JdbcValues.date(resultSet, "travel_date"));
        booking.setBookingStatus(resultSet.getString("booking_status"));
        booking.setTotalAmount(resultSet.getBigDecimal("total_amount"));
        return booking;
    }

    private RouteSummary mapRoute(ResultSet resultSet) throws SQLException {
        RouteSummary route = new RouteSummary();
        route.setSource(resultSet.getString("source_station"));
        route.setDestination(resultSet.getString("destination_station"));
        route.setBookings(resultSet.getLong("total"));
        return route;
    }

    private String randomPnr() {
        StringBuilder builder = new StringBuilder(10);
        builder.append(1 + RANDOM.nextInt(9));
        for (int index = 1; index < 10; index++) {
            builder.append(RANDOM.nextInt(10));
        }
        return builder.toString();
    }

    private record Filter(String where, List<Object> params) {
    }
}
