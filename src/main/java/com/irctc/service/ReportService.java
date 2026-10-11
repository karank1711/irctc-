package com.irctc.service;

import com.irctc.dao.BookingDAO;
import com.irctc.exception.ValidationException;
import com.irctc.model.ReportSummary;
import com.irctc.util.AppClock;
import com.irctc.util.Validators;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ReportService {
    public static final int EXPORT_LIMIT = 5000;
    private final BookingDAO bookingDAO = new BookingDAO();
    private final ActivityService activityService = new ActivityService();

    public ReportSummary build(LocalDate from, LocalDate to) throws ValidationException {
        DateRange range = validate(from, to);
        return bookingDAO.report(range.start(), range.end(), EXPORT_LIMIT);
    }

    public ReportSummary export(long adminId, LocalDate from, LocalDate to) throws ValidationException {
        DateRange range = validate(from, to);
        ReportSummary summary = bookingDAO.report(range.start(), range.end(), EXPORT_LIMIT);
        activityService.record(adminId, "REPORT_GENERATED",
                "Report generated (" + from + " to " + to + ")", "REPORT", null);
        return summary;
    }

    private DateRange validate(LocalDate from, LocalDate to) throws ValidationException {
        Validators validators = new Validators();
        if (from == null) {
            validators.add("from", "Start date is required.");
        }
        if (to == null) {
            validators.add("to", "End date is required.");
        }
        if (from != null && to != null && to.isBefore(from)) {
            validators.add("to", "End date must be on or after the start date.");
        }
        if (from != null && to != null && from.plusYears(2).isBefore(to)) {
            validators.add("to", "Choose a range of two years or less.");
        }
        validators.finish();
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.plusDays(1).atStartOfDay();
        return new DateRange(start, end);
    }

    public static LocalDate defaultFrom() {
        return AppClock.now().toLocalDate().minusDays(29);
    }

    public static LocalDate defaultTo() {
        return AppClock.now().toLocalDate();
    }

    private record DateRange(LocalDateTime start, LocalDateTime end) {
    }
}
