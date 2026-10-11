package com.irctc.service;

import com.irctc.dao.AdminDAO;
import com.irctc.dao.SettingsDAO;
import com.irctc.exception.DataAccessException;
import com.irctc.exception.ValidationException;
import com.irctc.model.Admin;
import com.irctc.util.DaoSupport;
import com.irctc.util.PasswordUtil;
import com.irctc.util.Validators;

import java.util.Map;

public class SettingsService {
    private final SettingsDAO settingsDAO = new SettingsDAO();
    private final AdminDAO adminDAO = new AdminDAO();
    private final ActivityService activityService = new ActivityService();

    public Map<String, String> all() {
        return settingsDAO.findAll();
    }

    public int pageSize() {
        try {
            String raw = settingsDAO.findAll().getOrDefault("page_size", "10");
            int size = Integer.parseInt(raw);
            if (size == 5 || size == 10 || size == 20) {
                return size;
            }
        } catch (NumberFormatException | DataAccessException ex) {
            return 10;
        }
        return 10;
    }

    public void savePreferences(long adminId, String portalName, String supportEmail, String pageSize) throws ValidationException {
        Validators validators = new Validators();
        validators.required("portalName", portalName, "Portal name");
        validators.maxLength("portalName", portalName, 80, "Portal name");
        validators.required("supportEmail", supportEmail, "Support email");
        validators.maxLength("supportEmail", supportEmail, 150, "Support email");
        validators.email("supportEmail", supportEmail);
        validators.oneOf("pageSize", pageSize, java.util.Set.of("5", "10", "20"), "Choose 5, 10, or 20 rows.");
        validators.finish();
        DaoSupport.transaction("Settings could not be saved.", connection -> {
            settingsDAO.upsert(connection, "portal_name", portalName);
            settingsDAO.upsert(connection, "support_email", supportEmail);
            settingsDAO.upsert(connection, "page_size", pageSize);
            activityService.record(connection, adminId, "SETTINGS_UPDATED", "Portal settings updated", "SETTINGS", null);
        });
    }

    public void changePassword(long adminId, String currentPassword, String newPassword, String confirmPassword)
            throws ValidationException {
        Validators validators = new Validators();
        validators.required("currentPassword", currentPassword, "Current password");
        validators.required("newPassword", newPassword, "New password");
        if (newPassword != null && (newPassword.length() < 8 || newPassword.length() > 72)) {
            validators.add("newPassword", "New password must be 8 to 72 characters.");
        }
        if (newPassword != null && !newPassword.equals(confirmPassword)) {
            validators.add("confirmPassword", "New password and confirmation do not match.");
        }
        validators.finish();
        Admin admin = adminDAO.findById(adminId);
        if (admin == null || !PasswordUtil.matches(currentPassword, admin.getPasswordHash())) {
            Validators again = new Validators();
            again.add("currentPassword", "Current password is incorrect.");
            again.finish();
        }
        String hash = PasswordUtil.hash(newPassword);
        DaoSupport.transaction("The password could not be changed.", connection -> {
            adminDAO.updatePassword(connection, adminId, hash);
            activityService.record(connection, adminId, "SETTINGS_UPDATED", "Administrator password changed", "ADMIN", adminId);
        });
    }
}
