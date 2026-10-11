package com.irctc.model;

import java.math.BigDecimal;

public class ServiceType {
    private String code;
    private String name;
    private String description;
    private boolean offered;
    private int sortOrder;
    private long bookingCount;
    private BigDecimal percent = BigDecimal.ZERO;
    private String color;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isOffered() {
        return offered;
    }

    public void setOffered(boolean offered) {
        this.offered = offered;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public long getBookingCount() {
        return bookingCount;
    }

    public void setBookingCount(long bookingCount) {
        this.bookingCount = bookingCount;
    }

    public BigDecimal getPercent() {
        return percent;
    }

    public void setPercent(BigDecimal percent) {
        this.percent = percent;
    }

    public String getPercentLabel() {
        return percent == null ? "0.0%" : percent.toPlainString() + "%";
    }

    public String getCountLabel() {
        return com.irctc.util.Formatters.integer(bookingCount);
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getColor() {
        if (color != null && !color.isBlank()) {
            return color;
        }
        if (code == null) {
            return "#94a3b8";
        }
        return switch (code) {
            case "TRAIN_TICKET" -> "#2563eb";
            case "TATKAL" -> "#7c3aed";
            case "TOURISM" -> "#f59e0b";
            case "CATERING" -> "#14b8a6";
            default -> "#64748b";
        };
    }
}
