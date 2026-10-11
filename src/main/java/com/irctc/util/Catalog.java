package com.irctc.util;

import java.util.List;
import java.util.Set;

/** Values the portal accepts. Anything else is rejected before it reaches SQL. */
public final class Catalog {
    public static final Set<String> BOOKING_STATUSES = Set.of("CONFIRMED", "WAITING", "RAC", "CANCELLED");
    public static final Set<String> ACCOUNT_STATUSES = Set.of("ACTIVE", "INACTIVE");
    public static final Set<String> TRAVEL_CLASSES = Set.of("1A", "2A", "3A", "SL", "CC");
    public static final List<String> DAYS = List.of("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN");
    public static final Set<String> ACTIVITY_TYPES = Set.of(
            "BOOKING_CREATED", "BOOKING_CANCELLED", "USER_REGISTERED", "USER_UPDATED",
            "EMPLOYEE_ADDED", "EMPLOYEE_UPDATED", "TRAIN_UPDATED", "SCHEDULE_UPDATED",
            "REPORT_GENERATED", "SETTINGS_UPDATED", "ADMIN_LOGIN", "CONTENT_UPDATED", "PROFILE_UPDATED");
    public static final Set<String> ACTION_ICONS = Set.of(
            "ticket", "search", "users", "user", "train-front", "file-bar-chart",
            "calendar", "map-pin", "bell", "clipboard-list");

    private Catalog() {
    }

    public static boolean chartPeriod(int days) {
        return days == 7 || days == 30 || days == 90;
    }

    /** In-app links only. Blocks external URLs and path tricks. */
    public static boolean safeDestination(String path) {
        if (path == null || path.length() > 160 || !path.startsWith("/") || path.startsWith("//") || path.contains("..")) {
            return false;
        }
        return path.matches("^/(bookings|users|employees|trains|reports|services|activities|search|settings|manage|dashboard|profile)([/?#].*)?$");
    }

    public static boolean safeImageReference(String value) {
        if (value == null || value.isBlank() || value.length() > 240 || value.contains("..") || value.contains("\\")) {
            return false;
        }
        if (value.indexOf(' ') >= 0 || value.indexOf('"') >= 0 || value.indexOf('\'') >= 0 || value.indexOf('(') >= 0) {
            return false;
        }
        if (value.startsWith("/assets/images/") || value.matches("^/media/[A-Za-z0-9._-]+$")) {
            return true;
        }
        return value.startsWith("https://");
    }
}
