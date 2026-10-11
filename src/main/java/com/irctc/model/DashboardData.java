package com.irctc.model;

import java.util.List;

/** Everything the dashboard page needs, already loaded from the database. */
public class DashboardData {
    private List<MetricView> metrics;
    private ChartSeries bookingsChart;
    private List<ServiceType> services;
    private long offeredBookingTotal;
    private List<Booking> recentBookings;
    private List<RouteSummary> topRoutes;
    private List<ActivityLog> activities;
    private String generatedLabel;

    public List<MetricView> getMetrics() {
        return metrics;
    }

    public void setMetrics(List<MetricView> metrics) {
        this.metrics = metrics;
    }

    public ChartSeries getBookingsChart() {
        return bookingsChart;
    }

    public void setBookingsChart(ChartSeries bookingsChart) {
        this.bookingsChart = bookingsChart;
    }

    public List<ServiceType> getServices() {
        return services;
    }

    public void setServices(List<ServiceType> services) {
        this.services = services;
    }

    public long getOfferedBookingTotal() {
        return offeredBookingTotal;
    }

    public void setOfferedBookingTotal(long offeredBookingTotal) {
        this.offeredBookingTotal = offeredBookingTotal;
    }

    public String getOfferedBookingTotalLabel() {
        return com.irctc.util.Formatters.integer(offeredBookingTotal);
    }

    public List<Booking> getRecentBookings() {
        return recentBookings;
    }

    public void setRecentBookings(List<Booking> recentBookings) {
        this.recentBookings = recentBookings;
    }

    public List<RouteSummary> getTopRoutes() {
        return topRoutes;
    }

    public void setTopRoutes(List<RouteSummary> topRoutes) {
        this.topRoutes = topRoutes;
    }

    public List<ActivityLog> getActivities() {
        return activities;
    }

    public void setActivities(List<ActivityLog> activities) {
        this.activities = activities;
    }

    public String getGeneratedLabel() {
        return generatedLabel;
    }

    public void setGeneratedLabel(String generatedLabel) {
        this.generatedLabel = generatedLabel;
    }
}
