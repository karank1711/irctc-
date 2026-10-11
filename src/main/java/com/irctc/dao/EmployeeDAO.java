package com.irctc.dao;

import com.irctc.model.Employee;
import com.irctc.model.PageResult;
import com.irctc.util.DaoSupport;
import com.irctc.util.JdbcValues;
import com.irctc.util.Pagination;
import com.irctc.util.TextSearch;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class EmployeeDAO {
    private static final String COLUMNS =
            "id, employee_code, full_name, email, phone, department, designation, status, created_at, updated_at";

    public PageResult<Employee> search(String query, String status, int page, int pageSize) {
        return DaoSupport.query("Employees could not be loaded.", connection -> {
            String where = " WHERE (? = '' OR full_name LIKE ? ESCAPE '\\\\' OR email LIKE ? ESCAPE '\\\\'"
                    + " OR employee_code LIKE ? ESCAPE '\\\\' OR department LIKE ? ESCAPE '\\\\')"
                    + " AND (? = '' OR status = ?)";
            List<Object> filters = filters(query, status);
            int total = scalar(connection, "SELECT COUNT(*) FROM employees" + where, filters);
            int totalPages = Pagination.totalPages(total, pageSize);
            int safePage = Pagination.clamp(page, totalPages);
            List<Object> params = new ArrayList<>(filters);
            params.add(pageSize);
            params.add(Pagination.offset(safePage, pageSize));
            List<Employee> items = new ArrayList<>();
            String sql = "SELECT " + COLUMNS + " FROM employees" + where + " ORDER BY created_at DESC, id DESC LIMIT ? OFFSET ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                DaoSupport.bind(statement, params);
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        items.add(map(resultSet));
                    }
                }
            }
            return new PageResult<>(items, safePage, pageSize, total, totalPages);
        });
    }

    public Employee findById(long id) {
        return DaoSupport.query("The employee could not be loaded.", connection -> {
            try (PreparedStatement statement = connection.prepareStatement("SELECT " + COLUMNS + " FROM employees WHERE id = ?")) {
                statement.setLong(1, id);
                try (ResultSet resultSet = statement.executeQuery()) {
                    return resultSet.next() ? map(resultSet) : null;
                }
            }
        });
    }

    public boolean emailTaken(Connection connection, String email, long exceptId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT 1 FROM employees WHERE email = ? AND id <> ?")) {
            statement.setString(1, email);
            statement.setLong(2, exceptId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    public long insert(Connection connection, Employee employee) throws SQLException {
        String pendingCode = "TMP" + java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        String sql = "INSERT INTO employees (employee_code, full_name, email, phone, department, designation, status)"
                + " VALUES (?, ?, ?, ?, ?, ?, ?)";
        long id;
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, pendingCode);
            statement.setString(2, employee.getFullName());
            statement.setString(3, employee.getEmail());
            statement.setString(4, employee.getPhone());
            statement.setString(5, employee.getDepartment());
            statement.setString(6, employee.getDesignation());
            statement.setString(7, employee.getStatus());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("The employee could not be saved.");
                }
                id = keys.getLong(1);
            }
        }
        String code = "EMP" + (1000 + id);
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE employees SET employee_code = ? WHERE id = ?")) {
            statement.setString(1, code);
            statement.setLong(2, id);
            statement.executeUpdate();
        }
        return id;
    }

    public void update(Connection connection, Employee employee) throws SQLException {
        String sql = "UPDATE employees SET full_name = ?, email = ?, phone = ?, department = ?, designation = ?, status = ?"
                + " WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, employee.getFullName());
            statement.setString(2, employee.getEmail());
            statement.setString(3, employee.getPhone());
            statement.setString(4, employee.getDepartment());
            statement.setString(5, employee.getDesignation());
            statement.setString(6, employee.getStatus());
            statement.setLong(7, employee.getId());
            statement.executeUpdate();
        }
    }

    public void updateStatus(Connection connection, long id, String status) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("UPDATE employees SET status = ? WHERE id = ?")) {
            statement.setString(1, status);
            statement.setLong(2, id);
            statement.executeUpdate();
        }
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

    private Employee map(ResultSet resultSet) throws SQLException {
        Employee employee = new Employee();
        employee.setId(resultSet.getLong("id"));
        employee.setEmployeeCode(resultSet.getString("employee_code"));
        employee.setFullName(resultSet.getString("full_name"));
        employee.setEmail(resultSet.getString("email"));
        employee.setPhone(resultSet.getString("phone"));
        employee.setDepartment(resultSet.getString("department"));
        employee.setDesignation(resultSet.getString("designation"));
        employee.setStatus(resultSet.getString("status"));
        employee.setCreatedAt(JdbcValues.dateTime(resultSet, "created_at"));
        employee.setUpdatedAt(JdbcValues.dateTime(resultSet, "updated_at"));
        return employee;
    }
}
