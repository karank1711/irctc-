package com.irctc.controller;

import com.irctc.dao.ActivityLogDAO;
import com.irctc.exception.DataAccessException;
import com.irctc.model.ActivityLog;
import com.irctc.model.PageResult;
import com.irctc.service.ActivityService;
import com.irctc.service.SettingsService;
import com.irctc.util.Catalog;
import com.irctc.util.JsonWriter;
import com.irctc.util.Pagination;
import com.irctc.util.SessionKeys;
import com.irctc.util.Web;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ActivityController extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(ActivityController.class.getName());
    private final ActivityLogDAO activityLogDAO = new ActivityLogDAO();
    private final ActivityService activityService = new ActivityService();
    private final SettingsService settingsService = new SettingsService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            String type = Web.param(request, "type");
            if (!type.isEmpty() && !Catalog.ACTIVITY_TYPES.contains(type)) {
                type = "";
            }
            PageResult<?> result = activityLogDAO.search(type, Web.intParam(request, "page", 1), settingsService.pageSize());
            List<ActivityLog> typeOptions = new ArrayList<>();
            Catalog.ACTIVITY_TYPES.stream().sorted().forEach(code -> {
                ActivityLog option = new ActivityLog();
                option.setActivityType(code);
                typeOptions.add(option);
            });
            typeOptions.sort(Comparator.comparing(ActivityLog::getTypeLabel));
            request.setAttribute("result", result);
            request.setAttribute("type", type);
            request.setAttribute("types", typeOptions);
            request.setAttribute("links", Pagination.links("/activities", result.getPage(), result.getTotalPages(),
                    Web.query(Map.of("type", type))));
            request.setAttribute("pageTitle", "Activity");
            Web.forward(request, response, "activities");
        } catch (DataAccessException ex) {
            LOGGER.log(Level.SEVERE, "Activities could not be loaded", ex);
            request.setAttribute("errorTitle", "Activity unavailable");
            request.setAttribute("errorMessage", "Activity could not be loaded.");
            Web.forward(request, response, "error");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String path = Web.path(request);
        try {
            activityService.markAllRead();
            if ("/api/notifications/read".equals(path)) {
                JsonWriter.write(response, Map.of("ok", true));
                return;
            }
            Web.flash(request, SessionKeys.FLASH_SUCCESS, "Notifications marked as read.");
            Web.redirect(request, response, Web.safeReturnPath(request.getParameter("returnTo")));
        } catch (DataAccessException ex) {
            LOGGER.log(Level.SEVERE, "Notifications could not be updated", ex);
            if (path.startsWith("/api/")) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                JsonWriter.write(response, Map.of("error", "Notifications could not be updated."));
                return;
            }
            Web.flash(request, SessionKeys.FLASH_ERROR, "Notifications could not be updated.");
            Web.redirect(request, response, "/dashboard");
        }
    }
}
