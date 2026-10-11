package com.irctc.controller;

import com.irctc.dao.UserDAO;
import com.irctc.exception.DataAccessException;
import com.irctc.exception.ValidationException;
import com.irctc.model.PageResult;
import com.irctc.model.User;
import com.irctc.service.SettingsService;
import com.irctc.service.UserService;
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

public class UserController extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(UserController.class.getName());
    private final UserDAO userDAO = new UserDAO();
    private final UserService userService = new UserService();
    private final SettingsService settingsService = new SettingsService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            switch (Web.path(request)) {
                case "/users/new" -> showForm(request, response, blank(), Map.of(), false);
                case "/users/edit" -> showEdit(request, response);
                case "/users/view" -> showView(request, response);
                case "/users" -> showList(request, response);
                default -> response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (DataAccessException ex) {
            LOGGER.log(Level.SEVERE, "Users could not be loaded", ex);
            request.setAttribute("errorTitle", "Users unavailable");
            request.setAttribute("errorMessage", "Users could not be loaded.");
            Web.forward(request, response, "error");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            if ("/users/save".equals(Web.path(request))) {
                save(request, response);
            } else if ("/users/status".equals(Web.path(request))) {
                changeStatus(request, response);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (DataAccessException ex) {
            LOGGER.log(Level.SEVERE, "User change failed", ex);
            Web.flash(request, SessionKeys.FLASH_ERROR, "The user could not be saved.");
            Web.redirect(request, response, "/users");
        }
    }

    private void showList(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String query = Web.param(request, "q");
        String status = allowedStatus(Web.param(request, "status"));
        PageResult<User> result = userDAO.search(query, status, Web.intParam(request, "page", 1), settingsService.pageSize());
        request.setAttribute("result", result);
        request.setAttribute("query", query);
        request.setAttribute("status", status);
        request.setAttribute("links", Pagination.links("/users", result.getPage(), result.getTotalPages(),
                Web.query(Map.of("q", query, "status", status))));
        request.setAttribute("pageTitle", "Users");
        Web.forward(request, response, "users");
    }

    private void showEdit(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User user = find(request);
        if (user == null) {
            missing(request, response);
            return;
        }
        showForm(request, response, user, Map.of(), true);
    }

    private void showView(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User user = find(request);
        if (user == null) {
            missing(request, response);
            return;
        }
        request.setAttribute("user", user);
        request.setAttribute("pageTitle", user.getFullName());
        Web.forward(request, response, "user-details");
    }

    private void showForm(HttpServletRequest request, HttpServletResponse response, User form, Map<String, String> errors, boolean editing)
            throws ServletException, IOException {
        request.setAttribute("form", form);
        request.setAttribute("errors", errors);
        request.setAttribute("editing", editing);
        request.setAttribute("pageTitle", editing ? "Edit user" : "Add user");
        Web.forward(request, response, "user-form");
    }

    private void save(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User form = read(request);
        try {
            userService.save(Web.adminId(request), form);
            Web.flash(request, SessionKeys.FLASH_SUCCESS, "User saved.");
            Web.redirect(request, response, "/users");
        } catch (ValidationException ex) {
            showForm(request, response, form, ex.getFieldErrors(), form.getId() > 0);
        }
    }

    private void changeStatus(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Long id = Web.longParam(request, "id");
        String status = Web.param(request, "status");
        if (id != null && userService.setStatus(Web.adminId(request), id, status)) {
            Web.flash(request, SessionKeys.FLASH_SUCCESS, "User status updated.");
        } else {
            Web.flash(request, SessionKeys.FLASH_ERROR, "The user status could not be changed.");
        }
        Web.redirect(request, response, "/users");
    }

    private User find(HttpServletRequest request) {
        Long id = Web.longParam(request, "id");
        return id == null ? null : userDAO.findById(id);
    }

    private User read(HttpServletRequest request) {
        User user = new User();
        Long id = Web.longParam(request, "id");
        user.setId(id == null ? 0 : id);
        user.setFullName(Web.param(request, "fullName"));
        user.setEmail(Web.param(request, "email"));
        user.setPhone(Web.param(request, "phone"));
        user.setStatus(Web.param(request, "status"));
        return user;
    }

    private User blank() {
        User user = new User();
        user.setStatus("ACTIVE");
        return user;
    }

    private String allowedStatus(String status) {
        return Catalog.ACCOUNT_STATUSES.contains(status) ? status : "";
    }

    private void missing(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Web.flash(request, SessionKeys.FLASH_ERROR, "That user was not found.");
        Web.redirect(request, response, "/users");
    }
}
