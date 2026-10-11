package com.irctc.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Turns database values into the text shown on the pages. */
public final class Formatters {
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH);
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a", Locale.ENGLISH);
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH);
    private static final DateTimeFormatter CHART_DAY = DateTimeFormatter.ofPattern("dd MMM", Locale.ENGLISH);

    private Formatters() {
    }

    public static String integer(long value) {
        return groupIndian(Long.toString(Math.abs(value)), value < 0);
    }

    public static String rupees(BigDecimal amount) {
        BigDecimal value = amount == null ? BigDecimal.ZERO : amount;
        BigDecimal crore = new BigDecimal("10000000");
        BigDecimal lakh = new BigDecimal("100000");
        if (value.abs().compareTo(crore) >= 0) {
            return "\u20B9" + value.divide(crore, 2, RoundingMode.HALF_UP).toPlainString() + " Cr";
        }
        if (value.abs().compareTo(lakh) >= 0) {
            return "\u20B9" + value.divide(lakh, 2, RoundingMode.HALF_UP).toPlainString() + " L";
        }
        return "\u20B9" + groupIndian(value.setScale(2, RoundingMode.HALF_UP).toPlainString(), value.signum() < 0);
    }

    public static String date(LocalDate date) {
        return date == null ? "" : DATE.format(date);
    }

    public static String dateTime(LocalDateTime dateTime) {
        return dateTime == null ? "" : DATE_TIME.format(dateTime);
    }

    public static String time(LocalTime time) {
        return time == null ? "—" : TIME.format(time);
    }

    public static String chartDay(LocalDate date) {
        return CHART_DAY.format(date);
    }

    public static String relative(LocalDateTime when) {
        if (when == null) {
            return "";
        }
        Duration duration = Duration.between(when, AppClock.now());
        long minutes = Math.max(0, duration.toMinutes());
        if (minutes < 1) {
            return "Just now";
        }
        if (minutes < 60) {
            return minutes + (minutes == 1 ? " min ago" : " mins ago");
        }
        long hours = duration.toHours();
        if (hours < 24) {
            return hours + (hours == 1 ? " hour ago" : " hours ago");
        }
        long days = duration.toDays();
        return days + (days == 1 ? " day ago" : " days ago");
    }

    public static String statusLabel(String status) {
        if (status == null) {
            return "";
        }
        return switch (status) {
            case "CONFIRMED" -> "Confirmed";
            case "WAITING" -> "Waiting";
            case "RAC" -> "RAC";
            case "CANCELLED" -> "Cancelled";
            case "ACTIVE" -> "Active";
            case "INACTIVE" -> "Inactive";
            default -> status;
        };
    }

    static String groupIndian(String plain, boolean negative) {
        String unsigned = plain.startsWith("-") ? plain.substring(1) : plain;
        int dot = unsigned.indexOf('.');
        String fraction = dot >= 0 ? unsigned.substring(dot) : "";
        String digits = dot >= 0 ? unsigned.substring(0, dot) : unsigned;
        String grouped;
        if (digits.length() <= 3) {
            grouped = digits;
        } else {
            String tail = digits.substring(digits.length() - 3);
            String head = digits.substring(0, digits.length() - 3);
            StringBuilder builder = new StringBuilder();
            while (head.length() > 2) {
                builder.insert(0, "," + head.substring(head.length() - 2));
                head = head.substring(0, head.length() - 2);
            }
            builder.insert(0, head);
            grouped = builder + "," + tail;
        }
        return (negative ? "-" : "") + grouped + fraction;
    }
}
