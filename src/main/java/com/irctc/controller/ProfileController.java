package com.irctc.controller;

import com.irctc.dao.AdminDAO;
import com.irctc.exception.DataAccessException;
import com.irctc.exception.ValidationException;
import com.irctc.model.Admin;
import com.irctc.service.ActivityService;
import com.irctc.util.DaoSupport;
import com.irctc.util.Formatters;
import com.irctc.util.SessionKeys;
import com.irctc.util.Validators;
import com.irctc.util.Web;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Shows the signed-in administrator and lets them change their display name. */
public class ProfileController extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(ProfileController.class.getName());
    private final AdminDAO adminDAO = new AdminDAO();
    private final ActivityService activityService = new ActivityService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        show(request, response, Web.param(request, "fullName"), Map.of());
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String fullName = Web.param(request, "fullName");
        try {
            Validators validators = new Validators();
            validators.required("fullName", fullName, "Full name");
            validators.maxLength("fullName", fullName, 100, "Full name");
            validators.finish();
            long adminId = Web.adminId(request);
            DaoSupport.transaction("The profile could not be saved.", connection -> {
                adminDAO.updateName(connection, adminId, fullName.trim());
                activityService.record(connection, adminId, "PROFILE_UPDATED", "Administrator profile updated", "ADMIN", adminId);
            });
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.setAttribute(SessionKeys.ADMIN_NAME, fullName.trim());
            }
            Web.flash(request, SessionKeys.FLASH_SUCCESS, "Profile updated.");
            Web.redirect(request, response, "/profile");
        } catch (ValidationException ex) {
            show(request, response, fullName, ex.getFieldErrors());
        } catch (DataAccessException ex) {
            LOGGER.log(Level.SEVERE, "Profile update failed", ex);
            Web.flash(request, SessionKeys.FLASH_ERROR, "The profile could not be saved.");
            Web.redirect(request, response, "/profile");
        }
    }

    private void show(HttpServletRequest request, HttpServletResponse response, String postedName, Map<String, String> errors)
            throws ServletException, IOException {
        try {
            Admin admin = adminDAO.findById(Web.adminId(request));
            if (admin == null) {
                Web.redirect(request, response, "/login");
                return;
            }
            request.setAttribute("admin", admin);
            request.setAttribute("fullNameInput", postedName == null || postedName.isBlank() ? admin.getFullName() : postedName);
            request.setAttribute("lastLoginLabel", admin.getLastLoginAt() == null ? "Not recorded" : Formatters.dateTime(admin.getLastLoginAt()));
            request.setAttribute("errors", errors);
            request.setAttribute("pageTitle", "My Profile");
            Web.forward(request, response, "profile");
        } catch (DataAccessException ex) {
            LOGGER.log(Level.SEVERE, "Profile could not be loaded", ex);
            request.setAttribute("errorTitle", "Profile unavailable");
            request.setAttribute("errorMessage", "The profile could not be loaded.");
            Web.forward(request, response, "error");
        }
    }
}
