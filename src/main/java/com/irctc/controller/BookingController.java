package com.irctc.controller;

import com.irctc.dao.BookingDAO;
import com.irctc.dao.DashboardDAO;
import com.irctc.dao.TrainDAO;
import com.irctc.dao.UserDAO;
import com.irctc.exception.DataAccessException;
import com.irctc.exception.ValidationException;
import com.irctc.model.Booking;
import com.irctc.model.PageResult;
import com.irctc.service.BookingService;
import com.irctc.service.SettingsService;
import com.irctc.util.Catalog;
import com.irctc.util.Pagination;
import com.irctc.util.SessionKeys;
import com.irctc.util.Web;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BookingController extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(BookingController.class.getName());
    private final BookingDAO bookingDAO = new BookingDAO();
    private final UserDAO userDAO = new UserDAO();
    private final TrainDAO trainDAO = new TrainDAO();
    private final DashboardDAO dashboardDAO = new DashboardDAO();
    private final BookingService bookingService = new BookingService();
    private final SettingsService settingsService = new SettingsService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            switch (Web.path(request)) {
                case "/bookings/new" -> showForm(request, response, new Booking(), Map.of());
                case "/bookings/view" -> showDetails(request, response);
                case "/bookings" -> showList(request, response);
                default -> response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (DataAccessException ex) {
            LOGGER.log(Level.SEVERE, "Bookings could not be loaded", ex);
            fail(request, response, "Bookings could not be loaded.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            switch (Web.path(request)) {
                case "/bookings/save" -> save(request, response);
                case "/bookings/cancel" -> cancel(request, response);
                default -> response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (DataAccessException ex) {
            LOGGER.log(Level.SEVERE, "Booking change failed", ex);
            Web.flash(request, SessionKeys.FLASH_ERROR, "The booking could not be saved.");
            Web.redirect(request, response, "/bookings");
        }
    }

    private void showList(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String query = Web.param(request, "q");
        String status = Web.param(request, "status");
        if (!status.isEmpty() && !Catalog.BOOKING_STATUSES.contains(status)) {
            status = "";
        }
        PageResult<Booking> result = bookingDAO.search(query, status, Web.intParam(request, "page", 1), settingsService.pageSize());
        request.setAttribute("result", result);
        request.setAttribute("query", query);
        request.setAttribute("status", status);
        request.setAttribute("links", Pagination.links("/bookings", result.getPage(), result.getTotalPages(),
                Web.query(Map.of("q", query, "status", status))));
        request.setAttribute("pageTitle", "Bookings");
        Web.forward(request, response, "bookings");
    }

    private void showDetails(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Long id = Web.longParam(request, "id");
        Booking booking = id == null ? null : bookingDAO.findById(id);
        if (booking == null) {
            Web.flash(request, SessionKeys.FLASH_ERROR, "That booking was not found.");
            Web.redirect(request, response, "/bookings");
            return;
        }
        request.setAttribute("booking", booking);
        request.setAttribute("pageTitle", "Booking " + booking.getPnr());
        Web.forward(request, response, "booking-details");
    }

    private void showForm(HttpServletRequest request, HttpServletResponse response, Booking form, Map<String, String> errors)
            throws ServletException, IOException {
        request.setAttribute("form", form);
        request.setAttribute("errors", errors);
        request.setAttribute("users", userDAO.activeUsers());
        request.setAttribute("trains", trainDAO.all());
        request.setAttribute("stations", trainDAO.stations());
        request.setAttribute("services", dashboardDAO.serviceBreakdown());
        request.setAttribute("pageTitle", "Record booking");
        Web.forward(request, response, "booking-form");
    }

    private void save(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        Booking form = readForm(request);
        try {
            long id = bookingService.create(Web.adminId(request), form);
            Web.flash(request, SessionKeys.FLASH_SUCCESS, "Booking " + form.getPnr() + " was recorded.");
            Web.redirect(request, response, "/bookings/view?id=" + id);
        } catch (ValidationException ex) {
            showForm(request, response, form, ex.getFieldErrors());
        }
    }

    private void cancel(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Long id = Web.longParam(request, "id");
        if (id != null && bookingService.cancel(Web.adminId(request), id)) {
            Web.flash(request, SessionKeys.FLASH_SUCCESS, "Booking cancelled.");
            Web.redirect(request, response, "/bookings/view?id=" + id);
            return;
        }
        Web.flash(request, SessionKeys.FLASH_ERROR, "That booking could not be cancelled.");
        Web.redirect(request, response, id == null ? "/bookings" : "/bookings/view?id=" + id);
    }

    private Booking readForm(HttpServletRequest request) {
        Booking booking = new Booking();
        Long userId = Web.longParam(request, "userId");
        Long trainId = Web.longParam(request, "trainId");
        booking.setUserId(userId == null ? 0 : userId);
        booking.setTrainId(trainId == null ? 0 : trainId);
        booking.setSourceStation(Web.param(request, "source").toUpperCase());
        booking.setDestinationStation(Web.param(request, "destination").toUpperCase());
        booking.setTravelClass(Web.param(request, "travelClass"));
        booking.setServiceType(Web.param(request, "serviceType"));
        booking.setBookingStatus(Web.param(request, "status"));
        booking.setTotalAmount(Web.money(Web.param(request, "amount")));
        String travelDate = Web.param(request, "travelDate");
        if (!travelDate.isEmpty()) {
            try {
                booking.setTravelDate(LocalDate.parse(travelDate));
            } catch (DateTimeParseException ex) {
                booking.setTravelDate(null);
            }
        }
        return booking;
    }

    private void fail(HttpServletRequest request, HttpServletResponse response, String message) throws ServletException, IOException {
        request.setAttribute("errorTitle", "Bookings unavailable");
        request.setAttribute("errorMessage", message);
        Web.forward(request, response, "error");
    }
}
