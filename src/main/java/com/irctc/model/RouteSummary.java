package com.irctc.model;

import com.irctc.util.Formatters;

public class RouteSummary {
    private String source;
    private String destination;
    private String sourceCity;
    private String destinationCity;
    private long bookings;

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getSourceCity() {
        return sourceCity;
    }

    public void setSourceCity(String sourceCity) {
        this.sourceCity = sourceCity;
    }

    public String getDestinationCity() {
        return destinationCity;
    }

    public void setDestinationCity(String destinationCity) {
        this.destinationCity = destinationCity;
    }

    public String getRouteLabel() {
        String from = sourceCity == null || sourceCity.isBlank() ? source : sourceCity;
        String to = destinationCity == null || destinationCity.isBlank() ? destination : destinationCity;
        return from + " → " + to;
    }

    public long getBookings() {
        return bookings;
    }

    public void setBookings(long bookings) {
        this.bookings = bookings;
    }

    public String getBookingsLabel() {
        return Formatters.integer(bookings);
    }
}
