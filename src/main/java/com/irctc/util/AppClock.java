package com.irctc.util;

import java.time.LocalDateTime;
import java.time.ZoneId;

/** One clock for the portal so dates stay in India Standard Time. */
public final class AppClock {
    public static final ZoneId ZONE = ZoneId.of("Asia/Kolkata");

    private AppClock() {
    }

    public static LocalDateTime now() {
        return LocalDateTime.now(ZONE);
    }
}
