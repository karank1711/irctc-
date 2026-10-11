package com.irctc.service;

import com.irctc.dao.ActivityLogDAO;
import com.irctc.dao.SettingsDAO;
import com.irctc.exception.DataAccessException;
import com.irctc.util.AppClock;
import com.irctc.util.Formatters;
import com.irctc.util.SessionKeys;
import com.irctc.util.Web;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.time.LocalDateTime;
import java.time.Year;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

/** Values needed by the sidebar and header on every protected page. */
public class LayoutService {
    private final SettingsDAO settingsDAO = new SettingsDAO();
    private final ActivityLogDAO activityLogDAO = new ActivityLogDAO();

    public void populate(HttpServletRequest request) {
        request.setAttribute("portalName", "IRCTC");
        request.setAttribute("currentYear", Year.now(AppClock.ZONE).getValue());
        request.setAttribute("currentPath", Web.path(request));
        HttpSession session = request.getSession(false);
        if (session != null) {
            request.setAttribute("adminName", session.getAttribute(SessionKeys.ADMIN_NAME));
            request.setAttribute("adminEmail", session.getAttribute(SessionKeys.ADMIN_EMAIL));
            request.setAttribute("csrfToken", session.getAttribute(SessionKeys.CSRF));
            Object loginTime = session.getAttribute(SessionKeys.LOGIN_TIME);
            if (loginTime != null) {
                try {
                    request.setAttribute("loginLabel", Formatters.dateTime(LocalDateTime.parse(loginTime.toString())));
                } catch (DateTimeParseException ex) {
                    request.setAttribute("loginLabel", "");
                }
            }
        }
        try {
            Map<String, String> settings = settingsDAO.findAll();
            request.setAttribute("portalName", settings.getOrDefault("portal_name", "IRCTC"));
            request.setAttribute("sampleData", "development_sample".equals(settings.get("data_mode")));
            request.setAttribute("dbHealthy", Boolean.TRUE);
            request.setAttribute("unreadCount", activityLogDAO.countUnread());
            request.setAttribute("headerNotifications", activityLogDAO.latest(6));
        } catch (DataAccessException ex) {
            request.setAttribute("dbHealthy", Boolean.FALSE);
            request.setAttribute("sampleData", Boolean.FALSE);
            request.setAttribute("unreadCount", 0);
            request.setAttribute("headerNotifications", List.of());
        }
    }
}
