package com.irctc.controller;

import com.irctc.dao.EmployeeDAO;
import com.irctc.exception.DataAccessException;
import com.irctc.exception.ValidationException;
import com.irctc.model.Employee;
import com.irctc.model.PageResult;
import com.irctc.service.EmployeeService;
import com.irctc.service.SettingsService;
import com.irctc.util.Catalog;
import com.irctc.util.Pagination;
import com.irctc.util.SessionKeys;
import com.irctc.util.Web;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EmployeeController extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(EmployeeController.class.getName());
    private final EmployeeDAO employeeDAO = new EmployeeDAO();
    private final EmployeeService employeeService = new EmployeeService();
    private final SettingsService settingsService = new SettingsService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            switch (Web.path(request)) {
                case "/employees/new" -> showForm(request, response, blank(), Map.of(), false);
                case "/employees/edit" -> edit(request, response);
                case "/employees/view" -> view(request, response);
                case "/employees" -> list(request, response);
                default -> response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (DataAccessException ex) {
            LOGGER.log(Level.SEVERE, "Employees could not be loaded", ex);
            request.setAttribute("errorTitle", "Employees unavailable");
            request.setAttribute("errorMessage", "Employees could not be loaded.");
            Web.forward(request, response, "error");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            if ("/employees/save".equals(Web.path(request))) {
                save(request, response);
            } else if ("/employees/status".equals(Web.path(request))) {
                status(request, response);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (DataAccessException ex) {
            LOGGER.log(Level.SEVERE, "Employee change failed", ex);
            Web.flash(request, SessionKeys.FLASH_ERROR, "The employee could not be saved.");
            Web.redirect(request, response, "/employees");
        }
    }

    private void list(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String query = Web.param(request, "q");
        String status = Catalog.ACCOUNT_STATUSES.contains(Web.param(request, "status")) ? Web.param(request, "status") : "";
        PageResult<Employee> result = employeeDAO.search(query, status, Web.intParam(request, "page", 1), settingsService.pageSize());
        request.setAttribute("result", result);
        request.setAttribute("query", query);
        request.setAttribute("status", status);
        request.setAttribute("links", Pagination.links("/employees", result.getPage(), result.getTotalPages(),
                Web.query(Map.of("q", query, "status", status))));
        request.setAttribute("pageTitle", "Employees");
        Web.forward(request, response, "employees");
    }

    private void edit(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Employee employee = find(request);
        if (employee == null) {
            missing(request, response);
            return;
        }
        showForm(request, response, employee, Map.of(), true);
    }

    private void view(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Employee employee = find(request);
        if (employee == null) {
            missing(request, response);
            return;
        }
        request.setAttribute("employee", employee);
        request.setAttribute("pageTitle", employee.getFullName());
        Web.forward(request, response, "employee-details");
    }

    private void showForm(HttpServletRequest request, HttpServletResponse response, Employee form,
                          Map<String, String> errors, boolean editing) throws ServletException, IOException {
        request.setAttribute("form", form);
        request.setAttribute("errors", errors);
        request.setAttribute("editing", editing);
        request.setAttribute("pageTitle", editing ? "Edit employee" : "Add employee");
        Web.forward(request, response, "employee-form");
    }

    private void save(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Employee form = read(request);
        try {
            employeeService.save(Web.adminId(request), form);
            Web.flash(request, SessionKeys.FLASH_SUCCESS, "Employee saved.");
            Web.redirect(request, response, "/employees");
        } catch (ValidationException ex) {
            showForm(request, response, form, ex.getFieldErrors(), form.getId() > 0);
        }
    }

    private void status(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Long id = Web.longParam(request, "id");
        if (id != null && employeeService.setStatus(Web.adminId(request), id, Web.param(request, "status"))) {
            Web.flash(request, SessionKeys.FLASH_SUCCESS, "Employee status updated.");
        } else {
            Web.flash(request, SessionKeys.FLASH_ERROR, "The employee status could not be changed.");
        }
        Web.redirect(request, response, "/employees");
    }

    private Employee find(HttpServletRequest request) {
        Long id = Web.longParam(request, "id");
        return id == null ? null : employeeDAO.findById(id);
    }

    private Employee read(HttpServletRequest request) {
        Employee employee = new Employee();
        Long id = Web.longParam(request, "id");
        employee.setId(id == null ? 0 : id);
        employee.setFullName(Web.param(request, "fullName"));
        employee.setEmail(Web.param(request, "email"));
        employee.setPhone(Web.param(request, "phone"));
        employee.setDepartment(Web.param(request, "department"));
        employee.setDesignation(Web.param(request, "designation"));
        employee.setStatus(Web.param(request, "status"));
        return employee;
    }

    private Employee blank() {
        Employee employee = new Employee();
        employee.setStatus("ACTIVE");
        return employee;
    }

    private void missing(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Web.flash(request, SessionKeys.FLASH_ERROR, "That employee was not found.");
        Web.redirect(request, response, "/employees");
    }
}
