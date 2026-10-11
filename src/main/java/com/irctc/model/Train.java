package com.irctc.model;

import com.irctc.util.Formatters;

import java.time.LocalDateTime;
import java.time.LocalTime;

public class Train {
    private long id;
    private String trainNumber;
    private String trainName;
    private long sourceStationId;
    private long destinationStationId;
    private String sourceCode;
    private String sourceName;
    private String destinationCode;
    private String destinationName;
    private LocalTime departureTime;
    private LocalTime arrivalTime;
    private String runningDays;
    private String status;
    private LocalDateTime createdAt;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTrainNumber() {
        return trainNumber;
    }

    public void setTrainNumber(String trainNumber) {
        this.trainNumber = trainNumber;
    }

    public String getTrainName() {
        return trainName;
    }

    public void setTrainName(String trainName) {
        this.trainName = trainName;
    }

    public long getSourceStationId() {
        return sourceStationId;
    }

    public void setSourceStationId(long sourceStationId) {
        this.sourceStationId = sourceStationId;
    }

    public long getDestinationStationId() {
        return destinationStationId;
    }

    public void setDestinationStationId(long destinationStationId) {
        this.destinationStationId = destinationStationId;
    }

    public String getSourceCode() {
        return sourceCode;
    }

    public void setSourceCode(String sourceCode) {
        this.sourceCode = sourceCode;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public String getDestinationCode() {
        return destinationCode;
    }

    public void setDestinationCode(String destinationCode) {
        this.destinationCode = destinationCode;
    }

    public String getDestinationName() {
        return destinationName;
    }

    public void setDestinationName(String destinationName) {
        this.destinationName = destinationName;
    }

    public String getRouteLabel() {
        return sourceCode + " → " + destinationCode;
    }

    public LocalTime getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(LocalTime departureTime) {
        this.departureTime = departureTime;
    }

    public String getDepartureLabel() {
        return Formatters.time(departureTime);
    }

    public LocalTime getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(LocalTime arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public String getArrivalLabel() {
        return Formatters.time(arrivalTime);
    }

    public String getRunningDays() {
        return runningDays;
    }

    public void setRunningDays(String runningDays) {
        this.runningDays = runningDays;
    }

    public boolean runsOn(String day) {
        return runningDays != null && runningDays.contains(day);
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatusLabel() {
        return Formatters.statusLabel(status);
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
