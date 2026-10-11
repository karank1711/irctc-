package com.irctc.dao;

import com.irctc.model.Announcement;
import com.irctc.model.QuickAction;
import com.irctc.model.ServiceType;
import com.irctc.util.DaoSupport;
import com.irctc.util.JdbcValues;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** Reads and writes dashboard content that is not calculated from bookings. */
public class ContentDAO {
    public List<QuickAction> actions(boolean visibleOnly) {
        return DaoSupport.query("Quick actions could not be loaded.", connection -> {
            String sql = """
                    SELECT id, label, icon_name, destination, visible, sort_order
                    FROM quick_actions
                    """ + (visibleOnly ? " WHERE visible = 1 " : "") + """
                    ORDER BY sort_order, id
                    """;
            List<QuickAction> actions = new ArrayList<>();
            try (PreparedStatement statement = connection.prepareStatement(sql);
                 ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    actions.add(mapAction(resultSet));
                }
            }
            return actions;
        });
    }

    public QuickAction findAction(long id) {
        return DaoSupport.query("The quick action could not be loaded.", connection -> {
            String sql = "SELECT id, label, icon_name, destination, visible, sort_order FROM quick_actions WHERE id = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, id);
                try (ResultSet resultSet = statement.executeQuery()) {
                    return resultSet.next() ? mapAction(resultSet) : null;
                }
            }
        });
    }

    public long saveAction(Connection connection, QuickAction action) throws SQLException {
        if (action.getId() > 0) {
            String sql = """
                    UPDATE quick_actions
                    SET label = ?, icon_name = ?, destination = ?, visible = ?, sort_order = ?
                    WHERE id = ?
                    """;
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                bindAction(statement, action);
                statement.setLong(6, action.getId());
                statement.executeUpdate();
            }
            return action.getId();
        }
        String sql = """
                INSERT INTO quick_actions (label, icon_name, destination, visible, sort_order)
                VALUES (?, ?, ?, ?, ?)
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bindAction(statement, action);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                return keys.next() ? keys.getLong(1) : 0L;
            }
        }
    }

    public void deleteAction(Connection connection, long id) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("DELETE FROM quick_actions WHERE id = ?")) {
            statement.setLong(1, id);
            statement.executeUpdate();
        }
    }

    public List<ServiceType> classes() {
        return DaoSupport.query("Booking classes could not be loaded.", connection -> {
            String sql = """
                    SELECT code, display_name, color_hex, visible, sort_order
                    FROM booking_classes ORDER BY sort_order, code
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
                    classes.add(travelClass);
                }
            }
            return classes;
        });
    }

    public void updateClass(Connection connection, ServiceType travelClass) throws SQLException {
        String sql = """
                UPDATE booking_classes
                SET display_name = ?, color_hex = ?, visible = ?, sort_order = ?
                WHERE code = ?
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, travelClass.getName());
            statement.setString(2, travelClass.getColor());
            statement.setBoolean(3, travelClass.isOffered());
            statement.setInt(4, travelClass.getSortOrder());
            statement.setString(5, travelClass.getCode());
            statement.executeUpdate();
        }
    }

    public List<Announcement> announcements() {
        return DaoSupport.query("Announcements could not be loaded.", connection -> queryAnnouncements(connection, false));
    }

    public List<Announcement> activeAnnouncements() {
        return DaoSupport.query("Announcements could not be loaded.", connection -> queryAnnouncements(connection, true));
    }

    public Announcement findAnnouncement(long id) {
        return DaoSupport.query("The announcement could not be loaded.", connection -> {
            String sql = "SELECT id, title, message, active, created_at FROM announcements WHERE id = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, id);
                try (ResultSet resultSet = statement.executeQuery()) {
                    return resultSet.next() ? mapAnnouncement(resultSet) : null;
                }
            }
        });
    }

    public void saveAnnouncement(Connection connection, Announcement announcement) throws SQLException {
        if (announcement.getId() > 0) {
            String sql = "UPDATE announcements SET title = ?, message = ?, active = ? WHERE id = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, announcement.getTitle());
                statement.setString(2, announcement.getMessage());
                statement.setBoolean(3, announcement.isActive());
                statement.setLong(4, announcement.getId());
                statement.executeUpdate();
            }
            return;
        }
        String sql = "INSERT INTO announcements (title, message, active) VALUES (?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, announcement.getTitle());
            statement.setString(2, announcement.getMessage());
            statement.setBoolean(3, announcement.isActive());
            statement.executeUpdate();
        }
    }

    public void deleteAnnouncement(Connection connection, long id) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("DELETE FROM announcements WHERE id = ?")) {
            statement.setLong(1, id);
            statement.executeUpdate();
        }
    }

    private List<Announcement> queryAnnouncements(Connection connection, boolean activeOnly) throws SQLException {
        String sql = """
                SELECT id, title, message, active, created_at FROM announcements
                """ + (activeOnly ? " WHERE active = 1 " : "") + " ORDER BY created_at DESC, id DESC";
        List<Announcement> announcements = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                announcements.add(mapAnnouncement(resultSet));
            }
        }
        return announcements;
    }

    private void bindAction(PreparedStatement statement, QuickAction action) throws SQLException {
        statement.setString(1, action.getLabel());
        statement.setString(2, action.getIconName());
        statement.setString(3, action.getDestination());
        statement.setBoolean(4, action.isVisible());
        statement.setInt(5, action.getSortOrder());
    }

    private QuickAction mapAction(ResultSet resultSet) throws SQLException {
        QuickAction action = new QuickAction();
        action.setId(resultSet.getLong("id"));
        action.setLabel(resultSet.getString("label"));
        action.setIconName(resultSet.getString("icon_name"));
        action.setDestination(resultSet.getString("destination"));
        action.setVisible(resultSet.getBoolean("visible"));
        action.setSortOrder(resultSet.getInt("sort_order"));
        return action;
    }

    private Announcement mapAnnouncement(ResultSet resultSet) throws SQLException {
        Announcement announcement = new Announcement();
        announcement.setId(resultSet.getLong("id"));
        announcement.setTitle(resultSet.getString("title"));
        announcement.setMessage(resultSet.getString("message"));
        announcement.setActive(resultSet.getBoolean("active"));
        announcement.setCreatedAt(JdbcValues.dateTime(resultSet, "created_at"));
        return announcement;
    }
}
