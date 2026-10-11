package com.irctc.controller;

import com.irctc.exception.DataAccessException;
import com.irctc.exception.ValidationException;
import com.irctc.service.SettingsService;
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

public class SettingsController extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(SettingsController.class.getName());
    private final SettingsService settingsService = new SettingsService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        show(request, response, Map.of());
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = Web.param(request, "action");
        try {
            if ("password".equals(action)) {
                settingsService.changePassword(Web.adminId(request), request.getParameter("currentPassword"),
                        request.getParameter("newPassword"), request.getParameter("confirmPassword"));
                Web.flash(request, SessionKeys.FLASH_SUCCESS, "Password changed. Use the new password next time you sign in.");
                Web.redirect(request, response, "/settings");
                return;
            }
            settingsService.savePreferences(Web.adminId(request), Web.param(request, "portalName"),
                    Web.param(request, "supportEmail"), Web.param(request, "pageSize"));
            Web.flash(request, SessionKeys.FLASH_SUCCESS, "Settings saved.");
            Web.redirect(request, response, "/settings");
        } catch (ValidationException ex) {
            show(request, response, ex.getFieldErrors());
        } catch (DataAccessException ex) {
            LOGGER.log(Level.SEVERE, "Settings could not be saved", ex);
            Web.flash(request, SessionKeys.FLASH_ERROR, "Settings could not be saved.");
            Web.redirect(request, response, "/settings");
        }
    }

    private void show(HttpServletRequest request, HttpServletResponse response, Map<String, String> errors)
            throws ServletException, IOException {
        try {
            Map<String, String> settings = settingsService.all();
            request.setAttribute("settings", settings);
            request.setAttribute("errors", errors);
            request.setAttribute("portalNameInput", first(request, "portalName", settings.get("portal_name")));
            request.setAttribute("supportEmailInput", first(request, "supportEmail", settings.get("support_email")));
            request.setAttribute("pageSizeInput", first(request, "pageSize", settings.getOrDefault("page_size", "10")));
            request.setAttribute("pageTitle", "Settings");
            Web.forward(request, response, "settings");
        } catch (DataAccessException ex) {
            LOGGER.log(Level.SEVERE, "Settings could not be loaded", ex);
            request.setAttribute("errorTitle", "Settings unavailable");
            request.setAttribute("errorMessage", "Settings could not be loaded.");
            Web.forward(request, response, "error");
        }
    }

    private String first(HttpServletRequest request, String name, String fallback) {
        String posted = request.getParameter(name);
        if (posted != null && "POST".equalsIgnoreCase(request.getMethod())) {
            return posted.trim();
        }
        return fallback == null ? "" : fallback;
    }
}
