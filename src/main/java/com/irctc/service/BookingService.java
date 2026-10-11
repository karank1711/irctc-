package com.irctc.service;

import com.irctc.dao.BookingDAO;
import com.irctc.dao.DashboardDAO;
import com.irctc.dao.TrainDAO;
import com.irctc.dao.UserDAO;
import com.irctc.exception.ValidationException;
import com.irctc.model.Booking;
import com.irctc.model.ServiceType;
import com.irctc.model.Station;
import com.irctc.model.Train;
import com.irctc.model.User;
import com.irctc.util.AppClock;
import com.irctc.util.Catalog;
import com.irctc.util.DaoSupport;
import com.irctc.util.Validators;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

public class BookingService {
    private final BookingDAO bookingDAO = new BookingDAO();
    private final UserDAO userDAO = new UserDAO();
    private final TrainDAO trainDAO = new TrainDAO();
    private final DashboardDAO dashboardDAO = new DashboardDAO();
    private final ActivityService activityService = new ActivityService();

    public long create(long adminId, Booking form) throws ValidationException {
        Validators validators = new Validators();
        if (form.getUserId() <= 0) {
            validators.add("userId", "Choose a passenger.");
        }
        if (form.getTrainId() <= 0) {
            validators.add("trainId", "Choose a train.");
        }
        validators.required("source", form.getSourceStation(), "Origin");
        validators.required("destination", form.getDestinationStation(), "Destination");
        if (form.getSourceStation() != null && form.getSourceStation().equalsIgnoreCase(form.getDestinationStation())) {
            validators.add("destination", "Origin and destination must be different.");
        }
        validators.oneOf("travelClass", form.getTravelClass(), Catalog.TRAVEL_CLASSES, "Choose a travel class.");
        validators.oneOf("serviceType", form.getServiceType(), offeredCodes(), "Choose an offered service.");
        validators.oneOf("status", form.getBookingStatus(), Catalog.BOOKING_STATUSES, "Choose a booking status.");
        validators.money("amount", form.getTotalAmount(), "Amount");
        LocalDate today = AppClock.now().toLocalDate();
        if (form.getTravelDate() == null) {
            validators.add("travelDate", "Travel date is required.");
        } else if (form.getTravelDate().isBefore(today.minusYears(1)) || form.getTravelDate().isAfter(today.plusYears(1))) {
            validators.add("travelDate", "Travel date must be within one year of today.");
        }
        validators.finish();

        Map<String, String> errors = new LinkedHashMap<>();
        long[] savedId = new long[1];
        DaoSupport.transaction("The booking could not be saved.", connection -> {
            User user = userDAO.findById(connection, form.getUserId());
            Train train = trainDAO.findById(connection, form.getTrainId());
            Station source = trainDAO.findStationByCode(connection, form.getSourceStation());
            Station destination = trainDAO.findStationByCode(connection, form.getDestinationStation());
            if (user == null || !"ACTIVE".equals(user.getStatus())) {
                errors.put("userId", "Choose an active passenger.");
            }
            if (train == null || !"ACTIVE".equals(train.getStatus())) {
                errors.put("trainId", "Choose an active train.");
            }
            if (source == null) {
                errors.put("source", "Choose a station that exists.");
            }
            if (destination == null) {
                errors.put("destination", "Choose a station that exists.");
            }
            if (!errors.isEmpty()) {
                return;
            }
            form.setSourceStation(source.getStationCode());
            form.setDestinationStation(destination.getStationCode());
            form.setPnr(bookingDAO.nextPnr(connection));
            form.setBookingDate(AppClock.now());
            savedId[0] = bookingDAO.insert(connection, form);
            activityService.record(connection, adminId, "BOOKING_CREATED",
                    "Booking created (PNR: " + form.getPnr() + ")", "BOOKING", savedId[0]);
        });
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
        return savedId[0];
    }

    public boolean cancel(long adminId, long bookingId) {
        boolean[] cancelled = {false};
        DaoSupport.transaction("The booking could not be cancelled.", connection -> {
            Booking booking = null;
            var found = bookingDAO.findById(bookingId);
            if (found != null && !"CANCELLED".equals(found.getBookingStatus())) {
                cancelled[0] = bookingDAO.cancel(connection, bookingId);
                if (cancelled[0]) {
                    activityService.record(connection, adminId, "BOOKING_CANCELLED",
                            "Booking cancelled (PNR: " + found.getPnr() + ")", "BOOKING", bookingId);
                }
            }
        });
        return cancelled[0];
    }

    private java.util.Set<String> offeredCodes() {
        java.util.Set<String> codes = new java.util.LinkedHashSet<>();
        for (ServiceType service : dashboardDAO.serviceBreakdown()) {
            if (service.isOffered()) {
                codes.add(service.getCode());
            }
        }
        return codes;
    }
}
