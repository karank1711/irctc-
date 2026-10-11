package com.irctc.model;

import com.irctc.util.Formatters;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Booking {
    private long id;
    private String pnr;
    private long userId;
    private String passengerName;
    private long trainId;
    private String trainNumber;
    private String trainName;
    private String sourceStation;
    private String destinationStation;
    private String travelClass;
    private String classLabel;
    private String serviceType;
    private String serviceName;
    private LocalDateTime bookingDate;
    private LocalDate travelDate;
    private String bookingStatus;
    private BigDecimal totalAmount;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPnr() {
        return pnr;
    }

    public void setPnr(String pnr) {
        this.pnr = pnr;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getPassengerName() {
        return passengerName;
    }

    public void setPassengerName(String passengerName) {
        this.passengerName = passengerName;
    }

    public long getTrainId() {
        return trainId;
    }

    public void setTrainId(long trainId) {
        this.trainId = trainId;
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

    public String getSourceStation() {
        return sourceStation;
    }

    public void setSourceStation(String sourceStation) {
        this.sourceStation = sourceStation;
    }

    public String getDestinationStation() {
        return destinationStation;
    }

    public void setDestinationStation(String destinationStation) {
        this.destinationStation = destinationStation;
    }

    public String getRouteLabel() {
        return sourceStation + " → " + destinationStation;
    }

    public String getTravelClass() {
        return travelClass;
    }

    public String getClassLabel() {
        return classLabel == null || classLabel.isBlank() ? travelClass : classLabel;
    }

    public void setClassLabel(String classLabel) {
        this.classLabel = classLabel;
    }

    public String getBookingDateLabel() {
        return bookingDate == null ? "" : Formatters.date(bookingDate.toLocalDate());
    }

    public void setTravelClass(String travelClass) {
        this.travelClass = travelClass;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public LocalDateTime getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(LocalDateTime bookingDate) {
        this.bookingDate = bookingDate;
    }

    public String getBookingTimeLabel() {
        return Formatters.dateTime(bookingDate);
    }

    public LocalDate getTravelDate() {
        return travelDate;
    }

    public void setTravelDate(LocalDate travelDate) {
        this.travelDate = travelDate;
    }

    public String getTravelDateLabel() {
        return Formatters.date(travelDate);
    }

    public String getBookingStatus() {
        return bookingStatus;
    }

    public void setBookingStatus(String bookingStatus) {
        this.bookingStatus = bookingStatus;
    }

    public String getStatusLabel() {
        return Formatters.statusLabel(bookingStatus);
    }

    public String getStatusTone() {
        if (bookingStatus == null) {
            return "neutral";
        }
        return switch (bookingStatus) {
            case "CONFIRMED" -> "confirmed";
            case "WAITING" -> "waiting";
            case "RAC" -> "rac";
            case "CANCELLED" -> "cancelled";
            default -> "neutral";
        };
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getAmountLabel() {
        return Formatters.rupees(totalAmount);
    }
}
