package com.irctc.controller;

import com.irctc.exception.DataAccessException;
import com.irctc.model.ChartSeries;
import com.irctc.model.DashboardData;
import com.irctc.model.ServiceType;
import com.irctc.service.ContentService;
import com.irctc.service.DashboardService;
import com.irctc.util.AppClock;
import com.irctc.util.Catalog;
import com.irctc.util.Formatters;
import com.irctc.util.JsonWriter;
import com.irctc.util.Web;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DashboardController extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(DashboardController.class.getName());
    private final DashboardService dashboardService = new DashboardService();
    private final ContentService contentService = new ContentService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String path = Web.path(request);
        try {
            if ("/api/dashboard/bookings".equals(path)) {
                int period = Web.intParam(request, "period", 7);
                if (!Catalog.chartPeriod(period)) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    JsonWriter.write(response, Map.of("error", "Choose 7, 30, or 90 days."));
                    return;
                }
                JsonWriter.write(response, chartBody(dashboardService.bookingsChart(period)));
                return;
            }
            DashboardData data = dashboardService.load();
            request.setAttribute("dashboard", data);
            request.setAttribute("banner", contentService.banner());
            request.setAttribute("quickActions", contentService.visibleActions());
            request.setAttribute("announcements", contentService.activeAnnouncements());
            request.setAttribute("bannerDate", Formatters.date(AppClock.now().toLocalDate()));
            request.setAttribute("chartJson", JsonWriter.stringify(chartBody(data.getBookingsChart())));
            request.setAttribute("serviceJson", JsonWriter.stringify(serviceBody(data.getServices())));
            request.setAttribute("pageTitle", "Dashboard");
            Web.forward(request, response, "dashboard");
        } catch (DataAccessException ex) {
            LOGGER.log(Level.SEVERE, "Dashboard data could not be loaded", ex);
            if (path.startsWith("/api/")) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                JsonWriter.write(response, Map.of("error", "Chart data could not be loaded."));
                return;
            }
            request.setAttribute("dashboardError", "The dashboard could not be loaded from the database.");
            request.setAttribute("pageTitle", "Dashboard");
            Web.forward(request, response, "dashboard");
        }
    }

    private Map<String, Object> chartBody(ChartSeries series) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("labels", series.getLabels());
        body.put("counts", series.getCounts());
        body.put("empty", series.isWithoutRows());
        return body;
    }

    private List<Map<String, Object>> serviceBody(List<ServiceType> services) {
        List<Map<String, Object>> segments = new ArrayList<>();
        for (ServiceType service : services) {
            if (!service.isOffered()) {
                continue;
            }
            Map<String, Object> segment = new LinkedHashMap<>();
            segment.put("label", service.getName());
            segment.put("count", service.getBookingCount());
            segment.put("color", service.getColor());
            segments.add(segment);
        }
        return segments;
    }
}
