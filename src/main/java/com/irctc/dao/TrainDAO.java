package com.irctc.dao;

import com.irctc.model.PageResult;
import com.irctc.model.ScheduleStop;
import com.irctc.model.Station;
import com.irctc.model.Train;
import com.irctc.util.DaoSupport;
import com.irctc.util.JdbcValues;
import com.irctc.util.Pagination;
import com.irctc.util.TextSearch;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

public class TrainDAO {
    private static final String SELECT = """
            SELECT t.id, t.train_number, t.train_name, t.source_station_id, t.destination_station_id,
                   src.station_code AS source_code, src.station_name AS source_name,
                   dest.station_code AS destination_code, dest.station_name AS destination_name,
                   t.departure_time, t.arrival_time, t.running_days, t.status, t.created_at
            FROM trains t
            JOIN stations src ON src.id = t.source_station_id
            JOIN stations dest ON dest.id = t.destination_station_id
            """;

    public PageResult<Train> search(String query, String status, int page, int pageSize) {
        return DaoSupport.query("Trains could not be loaded.", connection -> {
            String where = " WHERE (? = '' OR t.train_number LIKE ? ESCAPE '\\\\' OR t.train_name LIKE ? ESCAPE '\\\\'"
                    + " OR src.station_code LIKE ? ESCAPE '\\\\' OR dest.station_code LIKE ? ESCAPE '\\\\')"
                    + " AND (? = '' OR t.status = ?)";
            List<Object> filters = filters(query, status);
            int total = scalar(connection, "SELECT COUNT(*) FROM trains t JOIN stations src ON src.id = t.source_station_id"
                    + " JOIN stations dest ON dest.id = t.destination_station_id" + where, filters);
            int totalPages = Pagination.totalPages(total, pageSize);
            int safePage = Pagination.clamp(page, totalPages);
            List<Object> params = new ArrayList<>(filters);
            params.add(pageSize);
            params.add(Pagination.offset(safePage, pageSize));
            String sql = SELECT + where + " ORDER BY t.train_number LIMIT ? OFFSET ?";
            return new PageResult<>(list(connection, sql, params), safePage, pageSize, total, totalPages);
        });
    }

    public List<Train> all() {
        return DaoSupport.query("Trains could not be loaded.", connection ->
                list(connection, SELECT + " ORDER BY t.train_number", List.of()));
    }

    public Train findById(long id) {
        return DaoSupport.query("The train could not be loaded.", connection -> findById(connection, id));
    }

    public Train findById(Connection connection, long id) throws SQLException {
        List<Train> trains = list(connection, SELECT + " WHERE t.id = ?", List.of(id));
        return trains.isEmpty() ? null : trains.get(0);
    }

    public boolean numberTaken(Connection connection, String trainNumber, long exceptId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT 1 FROM trains WHERE train_number = ? AND id <> ?")) {
            statement.setString(1, trainNumber);
            statement.setLong(2, exceptId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    public long insert(Connection connection, Train train) throws SQLException {
        String sql = """
                INSERT INTO trains (train_number, train_name, source_station_id, destination_station_id,
                    departure_time, arrival_time, running_days, status)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bindTrain(statement, train);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
        }
        throw new SQLException("The train could not be saved.");
    }

    public void update(Connection connection, Train train) throws SQLException {
        String sql = """
                UPDATE trains
                SET train_number = ?, train_name = ?, source_station_id = ?, destination_station_id = ?,
                    departure_time = ?, arrival_time = ?, running_days = ?, status = ?
                WHERE id = ?
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            bindTrain(statement, train);
            statement.setLong(9, train.getId());
            statement.executeUpdate();
        }
    }

    public void updateStatus(Connection connection, long id, String status) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("UPDATE trains SET status = ? WHERE id = ?")) {
            statement.setString(1, status);
            statement.setLong(2, id);
            statement.executeUpdate();
        }
    }

    public List<Station> stations() {
        return DaoSupport.query("Stations could not be loaded.", connection -> {
            List<Station> stations = new ArrayList<>();
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT id, station_code, station_name, city, state_name FROM stations ORDER BY station_code")) {
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        stations.add(mapStation(resultSet));
                    }
                }
            }
            return stations;
        });
    }

    public Station findStation(Connection connection, long id) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT id, station_code, station_name, city, state_name FROM stations WHERE id = ?")) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? mapStation(resultSet) : null;
            }
        }
    }

    public Station findStationByCode(Connection connection, String code) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT id, station_code, station_name, city, state_name FROM stations WHERE station_code = ?")) {
            statement.setString(1, code);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? mapStation(resultSet) : null;
            }
        }
    }

    public List<ScheduleStop> schedule(long trainId) {
        return DaoSupport.query("The train schedule could not be loaded.", connection -> schedule(connection, trainId));
    }

    public List<ScheduleStop> schedule(Connection connection, long trainId) throws SQLException {
        String sql = """
                SELECT ts.id, ts.train_id, ts.station_id, s.station_code, s.station_name,
                       ts.stop_order, ts.arrival_time, ts.departure_time, ts.distance_km, ts.day_offset
                FROM train_schedules ts
                JOIN stations s ON s.id = ts.station_id
                WHERE ts.train_id = ?
                ORDER BY ts.stop_order
                """;
        List<ScheduleStop> stops = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, trainId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    stops.add(mapStop(resultSet));
                }
            }
        }
        return stops;
    }

    public boolean stopOrderTaken(Connection connection, long trainId, int stopOrder) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT 1 FROM train_schedules WHERE train_id = ? AND stop_order = ?")) {
            statement.setLong(1, trainId);
            statement.setInt(2, stopOrder);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    public void insertStop(Connection connection, ScheduleStop stop) throws SQLException {
        String sql = """
                INSERT INTO train_schedules (train_id, station_id, stop_order, arrival_time, departure_time, distance_km, day_offset)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, stop.getTrainId());
            statement.setLong(2, stop.getStationId());
            statement.setInt(3, stop.getStopOrder());
            setTime(statement, 4, stop.getArrivalTime());
            setTime(statement, 5, stop.getDepartureTime());
            statement.setBigDecimal(6, stop.getDistanceKm());
            statement.setInt(7, stop.getDayOffset());
            statement.executeUpdate();
        }
    }

    public boolean deleteStop(Connection connection, long trainId, long stopId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "DELETE FROM train_schedules WHERE train_id = ? AND id = ?")) {
            statement.setLong(1, trainId);
            statement.setLong(2, stopId);
            return statement.executeUpdate() > 0;
        }
    }

    private void bindTrain(PreparedStatement statement, Train train) throws SQLException {
        statement.setString(1, train.getTrainNumber());
        statement.setString(2, train.getTrainName());
        statement.setLong(3, train.getSourceStationId());
        statement.setLong(4, train.getDestinationStationId());
        statement.setTime(5, Time.valueOf(train.getDepartureTime()));
        statement.setTime(6, Time.valueOf(train.getArrivalTime()));
        statement.setString(7, train.getRunningDays());
        statement.setString(8, train.getStatus());
    }

    private List<Object> filters(String query, String status) {
        String like = query == null || query.isBlank() ? "%" : TextSearch.contains(query);
        String safeQuery = query == null ? "" : query.trim();
        String safeStatus = status == null ? "" : status.trim();
        return List.of(safeQuery, like, like, like, like, safeStatus, safeStatus);
    }

    private int scalar(Connection connection, String sql, List<Object> params) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            DaoSupport.bind(statement, params);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? resultSet.getInt(1) : 0;
            }
        }
    }

    private List<Train> list(Connection connection, String sql, List<Object> params) throws SQLException {
        List<Train> trains = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            DaoSupport.bind(statement, params);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    trains.add(mapTrain(resultSet));
                }
            }
        }
        return trains;
    }

    public Station findStation(long id) {
        return DaoSupport.query("The station could not be loaded.", connection -> findStation(connection, id));
    }

    public void updateStation(Connection connection, Station station) throws SQLException {
        String sql = "UPDATE stations SET station_name = ?, city = ?, state_name = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, station.getStationName());
            statement.setString(2, station.getCity());
            statement.setString(3, station.getStateName());
            statement.setLong(4, station.getId());
            statement.executeUpdate();
        }
    }

    private Train mapTrain(ResultSet resultSet) throws SQLException {
        Train train = new Train();
        train.setId(resultSet.getLong("id"));
        train.setTrainNumber(resultSet.getString("train_number"));
        train.setTrainName(resultSet.getString("train_name"));
        train.setSourceStationId(resultSet.getLong("source_station_id"));
        train.setDestinationStationId(resultSet.getLong("destination_station_id"));
        train.setSourceCode(resultSet.getString("source_code"));
        train.setSourceName(resultSet.getString("source_name"));
        train.setDestinationCode(resultSet.getString("destination_code"));
        train.setDestinationName(resultSet.getString("destination_name"));
        train.setDepartureTime(JdbcValues.time(resultSet, "departure_time"));
        train.setArrivalTime(JdbcValues.time(resultSet, "arrival_time"));
        train.setRunningDays(resultSet.getString("running_days"));
        train.setStatus(resultSet.getString("status"));
        train.setCreatedAt(JdbcValues.dateTime(resultSet, "created_at"));
        return train;
    }

    private Station mapStation(ResultSet resultSet) throws SQLException {
        Station station = new Station();
        station.setId(resultSet.getLong("id"));
        station.setStationCode(resultSet.getString("station_code"));
        station.setStationName(resultSet.getString("station_name"));
        station.setCity(resultSet.getString("city"));
        station.setStateName(resultSet.getString("state_name"));
        return station;
    }

    private ScheduleStop mapStop(ResultSet resultSet) throws SQLException {
        ScheduleStop stop = new ScheduleStop();
        stop.setId(resultSet.getLong("id"));
        stop.setTrainId(resultSet.getLong("train_id"));
        stop.setStationId(resultSet.getLong("station_id"));
        stop.setStationCode(resultSet.getString("station_code"));
        stop.setStationName(resultSet.getString("station_name"));
        stop.setStopOrder(resultSet.getInt("stop_order"));
        stop.setArrivalTime(JdbcValues.time(resultSet, "arrival_time"));
        stop.setDepartureTime(JdbcValues.time(resultSet, "departure_time"));
        stop.setDistanceKm(resultSet.getBigDecimal("distance_km"));
        stop.setDayOffset(resultSet.getInt("day_offset"));
        return stop;
    }

    private void setTime(PreparedStatement statement, int index, java.time.LocalTime time) throws SQLException {
        if (time == null) {
            statement.setNull(index, java.sql.Types.TIME);
        } else {
            statement.setTime(index, Time.valueOf(time));
        }
    }
}
