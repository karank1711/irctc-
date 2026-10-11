package com.irctc.service;

import com.irctc.dao.ActivityLogDAO;
import com.irctc.dao.BookingDAO;
import com.irctc.dao.DashboardDAO;
import com.irctc.dao.SettingsDAO;
import com.irctc.model.Booking;
import com.irctc.model.ChartSeries;
import com.irctc.model.DashboardData;
import com.irctc.model.MetricView;
import com.irctc.model.ServiceType;
import com.irctc.util.AppClock;
import com.irctc.util.Catalog;
import com.irctc.util.Formatters;
import com.irctc.util.PeriodComparison;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DashboardService {
    private final DashboardDAO dashboardDAO = new DashboardDAO();
    private final BookingDAO bookingDAO = new BookingDAO();
    private final ActivityLogDAO activityLogDAO = new ActivityLogDAO();
    private final SettingsDAO settingsDAO = new SettingsDAO();

    public DashboardData load() {
        DashboardData data = new DashboardData();
        data.setMetrics(List.of(
                bookingsMetric(),
                usersMetric(),
                trainsMetric(),
                revenueMetric()));
        data.setBookingsChart(bookingsChart(7));
        data.setServices(withPercents(dashboardDAO.classBreakdown()));
        data.setOfferedBookingTotal(offeredTotal(data.getServices()));
        data.setRecentBookings(bookingDAO.recent(5));
        applyClassLabels(data);
        data.setTopRoutes(dashboardDAO.topRoutes(5));
        data.setActivities(activityLogDAO.latest(activityLimit()));
        data.setGeneratedLabel(Formatters.dateTime(AppClock.now()));
        return data;
    }

    public ChartSeries bookingsChart(int days) {
        int period = Catalog.chartPeriod(days) ? days : 7;
        LocalDate today = AppClock.now().toLocalDate();
        LocalDate start = today.minusDays(period - 1L);
        Map<LocalDate, Long> values = dashboardDAO.dailyBookings(start.atStartOfDay(), today.plusDays(1).atStartOfDay());
        ChartSeries series = new ChartSeries();
        for (LocalDate day = start; !day.isAfter(today); day = day.plusDays(1)) {
            series.add(Formatters.chartDay(day), values.getOrDefault(day, 0L));
        }
        return series;
    }

    public List<ServiceType> services() {
        return withPercents(dashboardDAO.serviceBreakdown());
    }

    private MetricView bookingsMetric() {
        Window window = window();
        long total = dashboardDAO.totalBookings();
        PeriodComparison comparison = PeriodComparison.of(
                dashboardDAO.bookingsBetween(window.currentStart(), window.now()),
                dashboardDAO.bookingsBetween(window.previousStart(), window.currentStart()));
        return new MetricView("Total Bookings", "ticket", "blue", Formatters.integer(total), Long.toString(total),
                comparison, spark(dashboardDAO.dailyBookings(window.sparkStart(), window.sparkEnd())));
    }

    private MetricView usersMetric() {
        Window window = window();
        long total = dashboardDAO.activeUsers();
        PeriodComparison comparison = PeriodComparison.of(
                dashboardDAO.usersCreatedBetween(window.currentStart(), window.now()),
                dashboardDAO.usersCreatedBetween(window.previousStart(), window.currentStart()));
        return new MetricView("Active Passengers", "users", "green", Formatters.integer(total), Long.toString(total),
                comparison, spark(dashboardDAO.dailyUsers(window.sparkStart(), window.sparkEnd())));
    }

    private MetricView trainsMetric() {
        Window window = window();
        long total = dashboardDAO.totalTrains();
        PeriodComparison comparison = PeriodComparison.of(
                dashboardDAO.trainsCreatedBetween(window.currentStart(), window.now()),
                dashboardDAO.trainsCreatedBetween(window.previousStart(), window.currentStart()));
        return new MetricView("Total Trains", "train-front", "purple", Formatters.integer(total), Long.toString(total),
                comparison, spark(dashboardDAO.dailyTrains(window.sparkStart(), window.sparkEnd())));
    }

    private MetricView revenueMetric() {
        Window window = window();
        BigDecimal total = dashboardDAO.totalRevenue();
        PeriodComparison comparison = PeriodComparison.of(
                dashboardDAO.revenueBetween(window.currentStart(), window.now()),
                dashboardDAO.revenueBetween(window.previousStart(), window.currentStart()));
        Map<LocalDate, Long> daily = dashboardDAO.dailyRevenue(window.sparkStart(), window.sparkEnd());
        return new MetricView("Revenue Generated", "indian-rupee", "orange", Formatters.rupees(total),
                total.stripTrailingZeros().toPlainString(), comparison, spark(daily));
    }

    private List<Long> spark(Map<LocalDate, Long> values) {
        LocalDate today = AppClock.now().toLocalDate();
        List<Long> points = new ArrayList<>();
        for (LocalDate day = today.minusDays(6); !day.isAfter(today); day = day.plusDays(1)) {
            points.add(values.getOrDefault(day, 0L));
        }
        return points;
    }

    private void applyClassLabels(DashboardData data) {
        Map<String, String> names = new java.util.HashMap<>();
        for (ServiceType travelClass : data.getServices()) {
            names.put(travelClass.getCode(), travelClass.getName());
        }
        for (Booking booking : data.getRecentBookings()) {
            booking.setClassLabel(names.getOrDefault(booking.getTravelClass(), booking.getTravelClass()));
        }
    }

    private int activityLimit() {
        String raw = settingsDAO.findAll().getOrDefault("dashboard_activity_limit", "5");
        try {
            int limit = Integer.parseInt(raw);
            if (limit == 3 || limit == 5 || limit == 8) {
                return limit;
            }
        } catch (NumberFormatException ex) {
            return 5;
        }
        return 5;
    }

    private List<ServiceType> withPercents(List<ServiceType> services) {
        long total = offeredTotal(services);
        for (ServiceType service : services) {
            if (!service.isOffered() || total == 0) {
                service.setPercent(new BigDecimal("0.0"));
                continue;
            }
            service.setPercent(BigDecimal.valueOf(service.getBookingCount())
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(total), 1, RoundingMode.HALF_UP));
        }
        return services;
    }

    private long offeredTotal(List<ServiceType> services) {
        long total = 0;
        for (ServiceType service : services) {
            if (service.isOffered()) {
                total += service.getBookingCount();
            }
        }
        return total;
    }

    private Window window() {
        LocalDateTime now = AppClock.now();
        LocalDate today = now.toLocalDate();
        return new Window(now, now.minusDays(7), now.minusDays(14),
                today.minusDays(6).atStartOfDay(), today.plusDays(1).atStartOfDay());
    }

    private record Window(LocalDateTime now, LocalDateTime currentStart, LocalDateTime previousStart,
                          LocalDateTime sparkStart, LocalDateTime sparkEnd) {
    }
}
