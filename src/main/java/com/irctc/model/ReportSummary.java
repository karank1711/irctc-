package com.irctc.model;

import com.irctc.util.Formatters;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ReportSummary {
    private int totalBookings;
    private int confirmed;
    private int waiting;
    private int rac;
    private int cancelled;
    private BigDecimal revenue = BigDecimal.ZERO;
    private final List<RouteSummary> routes = new ArrayList<>();
    private final List<Booking> bookings = new ArrayList<>();

    public int getTotalBookings() {
        return totalBookings;
    }

    public void setTotalBookings(int totalBookings) {
        this.totalBookings = totalBookings;
    }

    public int getConfirmed() {
        return confirmed;
    }

    public void setConfirmed(int confirmed) {
        this.confirmed = confirmed;
    }

    public int getWaiting() {
        return waiting;
    }

    public void setWaiting(int waiting) {
        this.waiting = waiting;
    }

    public int getRac() {
        return rac;
    }

    public void setRac(int rac) {
        this.rac = rac;
    }

    public int getCancelled() {
        return cancelled;
    }

    public void setCancelled(int cancelled) {
        this.cancelled = cancelled;
    }

    public BigDecimal getRevenue() {
        return revenue;
    }

    public void setRevenue(BigDecimal revenue) {
        this.revenue = revenue;
    }

    public String getRevenueLabel() {
        return Formatters.rupees(revenue);
    }

    public List<RouteSummary> getRoutes() {
        return routes;
    }

    public List<Booking> getBookings() {
        return bookings;
    }
}
