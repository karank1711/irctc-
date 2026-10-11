package com.irctc.controller;

import com.irctc.dao.TrainDAO;
import com.irctc.exception.DataAccessException;
import com.irctc.exception.ValidationException;
import com.irctc.model.PageResult;
import com.irctc.model.ScheduleStop;
import com.irctc.model.Train;
import com.irctc.service.SettingsService;
import com.irctc.service.TrainService;
import com.irctc.util.Catalog;
import com.irctc.util.Pagination;
import com.irctc.util.SessionKeys;
import com.irctc.util.Web;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TrainController extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(TrainController.class.getName());
    private final TrainDAO trainDAO = new TrainDAO();
    private final TrainService trainService = new TrainService();
    private final SettingsService settingsService = new SettingsService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            switch (Web.path(request)) {
                case "/trains/new" -> showForm(request, response, blank(), Map.of(), false);
                case "/trains/edit" -> edit(request, response);
                case "/trains/view" -> view(request, response);
                case "/trains/schedule" -> schedule(request, response, Map.of());
                case "/trains" -> list(request, response);
                default -> response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (DataAccessException ex) {
            LOGGER.log(Level.SEVERE, "Trains could not be loaded", ex);
            request.setAttribute("errorTitle", "Trains unavailable");
            request.setAttribute("errorMessage", "Trains could not be loaded.");
            Web.forward(request, response, "error");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            switch (Web.path(request)) {
                case "/trains/save" -> save(request, response);
                case "/trains/status" -> status(request, response);
                case "/trains/schedule/save" -> saveStop(request, response);
                case "/trains/schedule/delete" -> deleteStop(request, response);
                default -> response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (DataAccessException ex) {
            LOGGER.log(Level.SEVERE, "Train change failed", ex);
            Web.flash(request, SessionKeys.FLASH_ERROR, "The train could not be saved.");
            Web.redirect(request, response, "/trains");
        }
    }

    private void list(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String query = Web.param(request, "q");
        String status = Catalog.ACCOUNT_STATUSES.contains(Web.param(request, "status")) ? Web.param(request, "status") : "";
        PageResult<Train> result = trainDAO.search(query, status, Web.intParam(request, "page", 1), settingsService.pageSize());
        request.setAttribute("result", result);
        request.setAttribute("query", query);
        request.setAttribute("status", status);
        request.setAttribute("links", Pagination.links("/trains", result.getPage(), result.getTotalPages(),
                Web.query(Map.of("q", query, "status", status))));
        request.setAttribute("pageTitle", "Train Management");
        Web.forward(request, response, "trains");
    }

    private void edit(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Train train = find(request);
        if (train == null) {
            missing(request, response);
            return;
        }
        showForm(request, response, train, Map.of(), true);
    }

    private void view(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Train train = find(request);
        if (train == null) {
            missing(request, response);
            return;
        }
        request.setAttribute("train", train);
        request.setAttribute("stops", trainDAO.schedule(train.getId()));
        request.setAttribute("pageTitle", train.getTrainNumber());
        Web.forward(request, response, "train-details");
    }

    private void schedule(HttpServletRequest request, HttpServletResponse response, Map<String, String> errors)
            throws ServletException, IOException {
        request.setAttribute("trains", trainDAO.all());
        request.setAttribute("stations", trainDAO.stations());
        request.setAttribute("errors", errors);
        Long trainId = Web.longParam(request, "trainId");
        if (trainId != null) {
            Train train = trainDAO.findById(trainId);
            request.setAttribute("selectedTrain", train);
            request.setAttribute("stops", train == null ? List.of() : trainDAO.schedule(trainId));
        }
        request.setAttribute("pageTitle", "Train schedule");
        Web.forward(request, response, "schedule");
    }

    private void showForm(HttpServletRequest request, HttpServletResponse response, Train form,
                          Map<String, String> errors, boolean editing) throws ServletException, IOException {
        request.setAttribute("form", form);
        request.setAttribute("errors", errors);
        request.setAttribute("editing", editing);
        request.setAttribute("stations", trainDAO.stations());
        request.setAttribute("days", Catalog.DAYS);
        request.setAttribute("pageTitle", editing ? "Edit train" : "Add train");
        Web.forward(request, response, "train-form");
    }

    private void save(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Train form = read(request);
        String[] days = request.getParameterValues("runningDays");
        try {
            trainService.save(Web.adminId(request), form, days == null ? List.of() : Arrays.asList(days));
            Web.flash(request, SessionKeys.FLASH_SUCCESS, "Train saved.");
            Web.redirect(request, response, "/trains");
        } catch (ValidationException ex) {
            showForm(request, response, form, ex.getFieldErrors(), form.getId() > 0);
        }
    }

    private void status(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Long id = Web.longParam(request, "id");
        if (id != null && trainService.setStatus(Web.adminId(request), id, Web.param(request, "status"))) {
            Web.flash(request, SessionKeys.FLASH_SUCCESS, "Train status updated.");
        } else {
            Web.flash(request, SessionKeys.FLASH_ERROR, "The train status could not be changed.");
        }
        Web.redirect(request, response, "/trains");
    }

    private void saveStop(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ScheduleStop stop = readStop(request);
        try {
            trainService.addStop(Web.adminId(request), stop);
            Web.flash(request, SessionKeys.FLASH_SUCCESS, "Schedule stop added.");
            Web.redirect(request, response, "/trains/schedule?trainId=" + stop.getTrainId());
        } catch (ValidationException ex) {
            request.setAttribute("stopForm", stop);
            schedule(request, response, ex.getFieldErrors());
        }
    }

    private void deleteStop(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Long trainId = Web.longParam(request, "trainId");
        Long stopId = Web.longParam(request, "stopId");
        if (trainId != null && stopId != null && trainService.deleteStop(Web.adminId(request), trainId, stopId)) {
            Web.flash(request, SessionKeys.FLASH_SUCCESS, "Schedule stop removed.");
        } else {
            Web.flash(request, SessionKeys.FLASH_ERROR, "The schedule stop could not be removed.");
        }
        Web.redirect(request, response, trainId == null ? "/trains/schedule" : "/trains/schedule?trainId=" + trainId);
    }

    private Train find(HttpServletRequest request) {
        Long id = Web.longParam(request, "id");
        return id == null ? null : trainDAO.findById(id);
    }

    private Train read(HttpServletRequest request) {
        Train train = new Train();
        Long id = Web.longParam(request, "id");
        Long source = Web.longParam(request, "sourceStationId");
        Long destination = Web.longParam(request, "destinationStationId");
        train.setId(id == null ? 0 : id);
        train.setTrainNumber(Web.param(request, "trainNumber"));
        train.setTrainName(Web.param(request, "trainName"));
        train.setSourceStationId(source == null ? 0 : source);
        train.setDestinationStationId(destination == null ? 0 : destination);
        train.setDepartureTime(time(Web.param(request, "departureTime")));
        train.setArrivalTime(time(Web.param(request, "arrivalTime")));
        train.setStatus(Web.param(request, "status"));
        return train;
    }

    private ScheduleStop readStop(HttpServletRequest request) {
        ScheduleStop stop = new ScheduleStop();
        Long trainId = Web.longParam(request, "trainId");
        Long stationId = Web.longParam(request, "stationId");
        stop.setTrainId(trainId == null ? 0 : trainId);
        stop.setStationId(stationId == null ? 0 : stationId);
        stop.setStopOrder(Web.intParam(request, "stopOrder", 0));
        stop.setDayOffset(Web.intParam(request, "dayOffset", 0));
        stop.setArrivalTime(time(Web.param(request, "arrivalTime")));
        stop.setDepartureTime(time(Web.param(request, "departureTime")));
        String distance = Web.param(request, "distanceKm");
        if (!distance.isEmpty()) {
            try {
                BigDecimal value = new BigDecimal(distance);
                stop.setDistanceKm(value.scale() > 1 ? null : value);
            } catch (NumberFormatException ex) {
                stop.setDistanceKm(null);
            }
        }
        return stop;
    }

    private LocalTime time(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalTime.parse(value);
        } catch (DateTimeParseException ex) {
            return null;
        }
    }

    private Train blank() {
        Train train = new Train();
        train.setStatus("ACTIVE");
        train.setRunningDays("MON,TUE,WED,THU,FRI,SAT");
        return train;
    }

    private void missing(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Web.flash(request, SessionKeys.FLASH_ERROR, "That train was not found.");
        Web.redirect(request, response, "/trains");
    }
}
