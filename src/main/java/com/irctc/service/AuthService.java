package com.irctc.service;

import com.irctc.dao.AdminDAO;
import com.irctc.model.Admin;
import com.irctc.util.EnvironmentConfig;
import com.irctc.util.PasswordUtil;

import java.util.Locale;
import java.util.logging.Logger;

public class AuthService {
    private static final Logger LOGGER = Logger.getLogger(AuthService.class.getName());
    private final AdminDAO adminDAO = new AdminDAO();

    /**
     * Creates the development administrator once.
     * If the email already exists, the stored password is left unchanged.
     */
    public void ensureDefaultAdmin() {
        EnvironmentConfig config = EnvironmentConfig.getInstance();
        String email = config.getOrDefault("ADMIN_EMAIL", "").toLowerCase(Locale.ROOT);
        String password = config.getOrDefault("ADMIN_PASSWORD", "");
        if (email.isBlank() || password.isBlank()) {
            LOGGER.warning("ADMIN_EMAIL or ADMIN_PASSWORD is not set. No administrator was created.");
            return;
        }
        if (password.length() > 72) {
            LOGGER.warning("ADMIN_PASSWORD is longer than 72 characters. No administrator was created.");
            return;
        }
        if (adminDAO.existsByEmail(email)) {
            return;
        }
        Admin admin = new Admin();
        admin.setFullName("Admin");
        admin.setEmail(email);
        admin.setPasswordHash(PasswordUtil.hash(password));
        admin.setActive(true);
        adminDAO.insert(admin);
        LOGGER.info("Created the initial administrator account.");
    }

    public Admin authenticate(String email, String password) {
        if (email == null || password == null || password.length() > 72) {
            return null;
        }
        Admin admin = adminDAO.findByEmail(email.trim().toLowerCase(Locale.ROOT));
        if (admin == null || !admin.isActive()) {
            return null;
        }
        if (!PasswordUtil.matches(password, admin.getPasswordHash())) {
            return null;
        }
        adminDAO.updateLastLogin(admin.getId());
        return admin;
    }
}
