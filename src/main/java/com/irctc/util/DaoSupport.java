package com.irctc.util;

import com.irctc.exception.DataAccessException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Opens connections and binds parameters so each DAO can focus on its SQL. */
public final class DaoSupport {
    private static final Logger LOGGER = Logger.getLogger(DaoSupport.class.getName());

    public interface Work<T> {
        T execute(Connection connection) throws SQLException;
    }

    public interface SqlTx {
        void execute(Connection connection) throws SQLException;
    }

    private DaoSupport() {
    }

    public static <T> T query(String safeMessage, Work<T> work) {
        try (Connection connection = DBConnection.getConnection()) {
            return work.execute(connection);
        } catch (SQLException | IllegalStateException ex) {
            LOGGER.log(Level.SEVERE, safeMessage, ex);
            throw new DataAccessException(safeMessage, ex);
        }
    }

    public static void transaction(String safeMessage, SqlTx work) {
        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                work.execute(connection);
                connection.commit();
            } catch (Exception ex) {
                connection.rollback();
                if (ex instanceof DataAccessException dataAccessException) {
                    throw dataAccessException;
                }
                LOGGER.log(Level.SEVERE, safeMessage, ex);
                throw new DataAccessException(safeMessage, ex);
            }
        } catch (SQLException | IllegalStateException ex) {
            LOGGER.log(Level.SEVERE, safeMessage, ex);
            throw new DataAccessException(safeMessage, ex);
        }
    }

    public static void bind(PreparedStatement statement, List<Object> params) throws SQLException {
        for (int index = 0; index < params.size(); index++) {
            Object value = params.get(index);
            int position = index + 1;
            if (value == null) {
                statement.setNull(position, Types.NULL);
            } else if (value instanceof String text) {
                statement.setString(position, text);
            } else if (value instanceof Integer number) {
                statement.setInt(position, number);
            } else if (value instanceof Long number) {
                statement.setLong(position, number);
            } else if (value instanceof java.math.BigDecimal number) {
                statement.setBigDecimal(position, number);
            } else if (value instanceof LocalDateTime dateTime) {
                statement.setTimestamp(position, java.sql.Timestamp.valueOf(dateTime));
            } else if (value instanceof LocalDate date) {
                statement.setDate(position, java.sql.Date.valueOf(date));
            } else {
                throw new SQLException("Unsupported query parameter.");
            }
        }
    }
}
