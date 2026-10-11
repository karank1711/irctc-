package com.irctc.controller;

import com.irctc.exception.DataAccessException;
import com.irctc.exception.ValidationException;
import com.irctc.model.Booking;
import com.irctc.model.ReportSummary;
import com.irctc.service.ReportService;
import com.irctc.util.Csv;
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

public class ReportController extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(ReportController.class.getName());
    private final ReportService reportService = new ReportService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        LocalDate from = dateOrDefault(request, "from", ReportService.defaultFrom());
        LocalDate to = dateOrDefault(request, "to", ReportService.defaultTo());
        if (from == null || to == null) {
            request.setAttribute("errors", Map.of("from", "Enter dates as YYYY-MM-DD."));
            request.setAttribute("from", Web.param(request, "from"));
            request.setAttribute("to", Web.param(request, "to"));
            request.setAttribute("pageTitle", "Reports");
            Web.forward(request, response, "reports");
            return;
        }
        try {
            ReportSummary summary = reportService.build(from, to);
            request.setAttribute("summary", summary);
            request.setAttribute("from", from.toString());
            request.setAttribute("to", to.toString());
            request.setAttribute("truncated", summary.getTotalBookings() > summary.getBookings().size());
            request.setAttribute("pageTitle", "Reports");
            Web.forward(request, response, "reports");
        } catch (ValidationException ex) {
            request.setAttribute("errors", ex.getFieldErrors());
            request.setAttribute("from", from.toString());
            request.setAttribute("to", to.toString());
            request.setAttribute("pageTitle", "Reports");
            Web.forward(request, response, "reports");
        } catch (DataAccessException ex) {
            LOGGER.log(Level.SEVERE, "Report failed", ex);
            request.setAttribute("errorTitle", "Report unavailable");
            request.setAttribute("errorMessage", "The report could not be loaded.");
            Web.forward(request, response, "error");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        if (!"/reports/export".equals(Web.path(request))) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        LocalDate from = dateOrDefault(request, "from", null);
        LocalDate to = dateOrDefault(request, "to", null);
        try {
            ReportSummary summary = reportService.export(Web.adminId(request), from, to);
            response.setContentType("text/csv; charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=\"bookings-" + from + "-to-" + to + ".csv\"");
            var writer = response.getWriter();
            writer.write(Csv.row("PNR", "Train number", "Train name", "Passenger", "From", "To", "Class",
                    "Service", "Booking time", "Travel date", "Status", "Amount"));
            for (Booking booking : summary.getBookings()) {
                writer.write(Csv.row(
                        booking.getPnr(), booking.getTrainNumber(), booking.getTrainName(), booking.getPassengerName(),
                        booking.getSourceStation(), booking.getDestinationStation(), booking.getTravelClass(),
                        booking.getServiceName(), booking.getBookingTimeLabel(), booking.getTravelDateLabel(),
                        booking.getStatusLabel(), booking.getTotalAmount() == null ? "" : booking.getTotalAmount().toPlainString()));
            }
            if (summary.getTotalBookings() > summary.getBookings().size()) {
                writer.write(Csv.row("Export limited to the newest " + ReportService.EXPORT_LIMIT + " rows."));
            }
        } catch (ValidationException ex) {
            Web.flash(request, SessionKeys.FLASH_ERROR, "Choose a valid date range before exporting.");
            Web.redirect(request, response, "/reports");
        } catch (DataAccessException ex) {
            LOGGER.log(Level.SEVERE, "CSV export failed", ex);
            Web.flash(request, SessionKeys.FLASH_ERROR, "The report could not be exported.");
            Web.redirect(request, response, "/reports");
        }
    }

    private LocalDate dateOrDefault(HttpServletRequest request, String name, LocalDate fallback) {
        String raw = Web.param(request, name);
        if (raw.isEmpty()) {
            return fallback;
        }
        try {
            return LocalDate.parse(raw);
        } catch (DateTimeParseException ex) {
            return null;
        }
    }
}
