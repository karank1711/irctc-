package com.irctc.service;

import com.irctc.dao.TrainDAO;
import com.irctc.exception.ValidationException;
import com.irctc.model.ScheduleStop;
import com.irctc.model.Station;
import com.irctc.model.Train;
import com.irctc.util.Catalog;
import com.irctc.util.DaoSupport;
import com.irctc.util.Validators;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TrainService {
    private final TrainDAO trainDAO = new TrainDAO();
    private final ActivityService activityService = new ActivityService();

    public void save(long adminId, Train train, List<String> selectedDays) throws ValidationException {
        Validators validators = new Validators();
        validators.required("trainNumber", train.getTrainNumber(), "Train number");
        if (train.getTrainNumber() != null && !train.getTrainNumber().matches("^[0-9]{4,5}$")) {
            validators.add("trainNumber", "Train number must be 4 or 5 digits.");
        }
        validators.required("trainName", train.getTrainName(), "Train name");
        validators.maxLength("trainName", train.getTrainName(), 120, "Train name");
        if (train.getSourceStationId() <= 0) {
            validators.add("sourceStationId", "Choose an origin station.");
        }
        if (train.getDestinationStationId() <= 0) {
            validators.add("destinationStationId", "Choose a destination station.");
        }
        if (train.getSourceStationId() > 0 && train.getSourceStationId() == train.getDestinationStationId()) {
            validators.add("destinationStationId", "Origin and destination must be different.");
        }
        if (train.getDepartureTime() == null) {
            validators.add("departureTime", "Departure time is required.");
        }
        if (train.getArrivalTime() == null) {
            validators.add("arrivalTime", "Arrival time is required.");
        }
        List<String> days = new ArrayList<>();
        if (selectedDays != null) {
            for (String day : Catalog.DAYS) {
                if (selectedDays.contains(day)) {
                    days.add(day);
                }
            }
        }
        if (days.isEmpty()) {
            validators.add("runningDays", "Choose at least one running day.");
        }
        validators.oneOf("status", train.getStatus(), Catalog.ACCOUNT_STATUSES, "Choose a status.");
        validators.finish();
        train.setRunningDays(String.join(",", days));

        Map<String, String> errors = new LinkedHashMap<>();
        DaoSupport.transaction("The train could not be saved.", connection -> {
            if (trainDAO.findStation(connection, train.getSourceStationId()) == null) {
                errors.put("sourceStationId", "Choose an origin station.");
            }
            if (trainDAO.findStation(connection, train.getDestinationStationId()) == null) {
                errors.put("destinationStationId", "Choose a destination station.");
            }
            if (trainDAO.numberTaken(connection, train.getTrainNumber(), train.getId())) {
                errors.put("trainNumber", "That train number is already used.");
            }
            if (!errors.isEmpty()) {
                return;
            }
            if (train.getId() == 0) {
                long id = trainDAO.insert(connection, train);
                activityService.record(connection, adminId, "TRAIN_UPDATED",
                        "Train added (" + train.getTrainNumber() + ")", "TRAIN", id);
            } else if (trainDAO.findById(connection, train.getId()) == null) {
                errors.put("form", "That train no longer exists.");
            } else {
                trainDAO.update(connection, train);
                activityService.record(connection, adminId, "TRAIN_UPDATED",
                        "Train updated (" + train.getTrainNumber() + ")", "TRAIN", train.getId());
            }
        });
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }

    public boolean setStatus(long adminId, long trainId, String status) {
        if (!Catalog.ACCOUNT_STATUSES.contains(status)) {
            return false;
        }
        boolean[] updated = {false};
        DaoSupport.transaction("The train status could not be changed.", connection -> {
            Train train = trainDAO.findById(connection, trainId);
            if (train == null) {
                return;
            }
            trainDAO.updateStatus(connection, trainId, status);
            updated[0] = true;
            String verb = "ACTIVE".equals(status) ? "activated" : "deactivated";
            activityService.record(connection, adminId, "TRAIN_UPDATED",
                    "Train " + verb + " (" + train.getTrainNumber() + ")", "TRAIN", trainId);
        });
        return updated[0];
    }

    public void addStop(long adminId, ScheduleStop stop) throws ValidationException {
        Validators validators = new Validators();
        if (stop.getTrainId() <= 0) {
            validators.add("trainId", "Choose a train.");
        }
        if (stop.getStationId() <= 0) {
            validators.add("stationId", "Choose a station.");
        }
        if (stop.getStopOrder() < 1 || stop.getStopOrder() > 30) {
            validators.add("stopOrder", "Stop order must be between 1 and 30.");
        }
        if (stop.getDayOffset() < 0 || stop.getDayOffset() > 5) {
            validators.add("dayOffset", "Day offset must be between 0 and 5.");
        }
        if (stop.getDistanceKm() == null || stop.getDistanceKm().compareTo(BigDecimal.ZERO) < 0
                || stop.getDistanceKm().compareTo(new BigDecimal("9999.9")) > 0) {
            validators.add("distanceKm", "Distance must be between 0 and 9999.9 km.");
        }
        validators.finish();

        Map<String, String> errors = new LinkedHashMap<>();
        DaoSupport.transaction("The schedule stop could not be saved.", connection -> {
            Train train = trainDAO.findById(connection, stop.getTrainId());
            Station station = trainDAO.findStation(connection, stop.getStationId());
            if (train == null) {
                errors.put("trainId", "Choose a train.");
            }
            if (station == null) {
                errors.put("stationId", "Choose a station.");
            }
            if (trainDAO.stopOrderTaken(connection, stop.getTrainId(), stop.getStopOrder())) {
                errors.put("stopOrder", "That stop order is already used on this train.");
            }
            if (!errors.isEmpty()) {
                return;
            }
            trainDAO.insertStop(connection, stop);
            activityService.record(connection, adminId, "SCHEDULE_UPDATED",
                    "Train schedule updated (" + train.getTrainNumber() + ")", "TRAIN", train.getId());
        });
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }

    public boolean deleteStop(long adminId, long trainId, long stopId) {
        boolean[] deleted = {false};
        DaoSupport.transaction("The schedule stop could not be removed.", connection -> {
            Train train = trainDAO.findById(connection, trainId);
            if (train == null) {
                return;
            }
            deleted[0] = trainDAO.deleteStop(connection, trainId, stopId);
            if (deleted[0]) {
                activityService.record(connection, adminId, "SCHEDULE_UPDATED",
                        "Train schedule updated (" + train.getTrainNumber() + ")", "TRAIN", trainId);
            }
        });
        return deleted[0];
    }
}
