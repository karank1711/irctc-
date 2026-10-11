package com.irctc.service;

import com.irctc.dao.UserDAO;
import com.irctc.exception.ValidationException;
import com.irctc.model.User;
import com.irctc.util.Catalog;
import com.irctc.util.DaoSupport;
import com.irctc.util.Validators;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class UserService {
    private final UserDAO userDAO = new UserDAO();
    private final ActivityService activityService = new ActivityService();

    public void save(long adminId, User user) throws ValidationException {
        validate(user);
        user.setEmail(user.getEmail().toLowerCase(Locale.ROOT));
        Map<String, String> errors = new LinkedHashMap<>();
        DaoSupport.transaction("The user could not be saved.", connection -> {
            if (userDAO.emailTaken(connection, user.getEmail(), user.getId())) {
                errors.put("email", "That email is already in use.");
                return;
            }
            if (user.getId() == 0) {
                long id = userDAO.insert(connection, user);
                activityService.record(connection, adminId, "USER_REGISTERED",
                        "User registered (" + user.getFullName() + ")", "USER", id);
            } else if (userDAO.findById(connection, user.getId()) == null) {
                errors.put("form", "That user no longer exists.");
            } else {
                userDAO.update(connection, user);
                activityService.record(connection, adminId, "USER_UPDATED",
                        "User updated (" + user.getFullName() + ")", "USER", user.getId());
            }
        });
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }

    public boolean setStatus(long adminId, long userId, String status) {
        if (!Catalog.ACCOUNT_STATUSES.contains(status)) {
            return false;
        }
        boolean[] updated = {false};
        DaoSupport.transaction("The user status could not be changed.", connection -> {
            User user = userDAO.findById(connection, userId);
            if (user == null) {
                return;
            }
            userDAO.updateStatus(connection, userId, status);
            updated[0] = true;
            activityService.record(connection, adminId, "USER_UPDATED",
                    "User " + status.toLowerCase(Locale.ROOT) + " (" + user.getFullName() + ")", "USER", userId);
        });
        return updated[0];
    }

    private void validate(User user) throws ValidationException {
        Validators validators = new Validators();
        validators.required("fullName", user.getFullName(), "Full name");
        validators.maxLength("fullName", user.getFullName(), 120, "Full name");
        validators.required("email", user.getEmail(), "Email");
        validators.maxLength("email", user.getEmail(), 150, "Email");
        validators.email("email", user.getEmail());
        validators.required("phone", user.getPhone(), "Mobile number");
        validators.phone("phone", user.getPhone());
        validators.oneOf("status", user.getStatus(), Catalog.ACCOUNT_STATUSES, "Choose a status.");
        validators.finish();
    }
}
