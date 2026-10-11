package com.irctc.controller;

import com.irctc.exception.DataAccessException;
import com.irctc.model.Admin;
import com.irctc.service.ActivityService;
import com.irctc.service.AuthService;
import com.irctc.util.SessionKeys;
import com.irctc.util.StartupStatus;
import com.irctc.util.Web;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AuthController extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(AuthController.class.getName());
    private final AuthService authService = new AuthService();
    private final ActivityService activityService = new ActivityService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if ("/logout".equals(Web.path(request))) {
            Web.redirect(request, response, "/dashboard");
            return;
        }
        request.setAttribute("startupError", StartupStatus.error());
        if ("1".equals(request.getParameter("loggedOut"))) {
            request.setAttribute("loginMessage", "You have signed out.");
        }
        Web.forward(request, response, "login");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if ("/logout".equals(Web.path(request))) {
            logout(request, response);
            return;
        }
        String email = Web.param(request, "email");
        String password = request.getParameter("password");
        if (email.isBlank() || password == null || password.isEmpty()) {
            request.setAttribute("loginError", "Email and password are required.");
            request.setAttribute("email", email);
            Web.forward(request, response, "login");
            return;
        }
        try {
            Admin admin = authService.authenticate(email, password);
            if (admin == null) {
                request.setAttribute("loginError", "Email or password is incorrect.");
                request.setAttribute("email", email);
                Web.forward(request, response, "login");
                return;
            }
            HttpSession existing = request.getSession(false);
            if (existing != null && !existing.isNew()) {
                request.changeSessionId();
            }
            HttpSession session = request.getSession(true);
            session.setAttribute(SessionKeys.ADMIN_ID, admin.getId());
            session.setAttribute(SessionKeys.ADMIN_NAME, admin.getFullName());
            session.setAttribute(SessionKeys.ADMIN_EMAIL, admin.getEmail());
            session.setAttribute(SessionKeys.LOGIN_TIME, com.irctc.util.AppClock.now().toString());
            session.setAttribute(SessionKeys.CSRF, Web.newToken());
            try {
                activityService.record(admin.getId(), "ADMIN_LOGIN", "Administrator signed in", "ADMIN", admin.getId());
            } catch (DataAccessException ex) {
                LOGGER.log(Level.WARNING, "Sign-in succeeded but the activity was not recorded", ex);
            }
            Web.redirect(request, response, "/dashboard");
        } catch (DataAccessException ex) {
            LOGGER.log(Level.SEVERE, "Sign-in could not reach the database", ex);
            request.setAttribute("loginError", "The database is unavailable. Try again later.");
            request.setAttribute("email", email);
            Web.forward(request, response, "login");
        }
    }

    private void logout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        Web.redirect(request, response, "/login?loggedOut=1");
    }
}
