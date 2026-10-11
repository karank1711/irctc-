package com.irctc.util;

/** Remembers a startup problem without exposing passwords or SQL text to the page. */
public final class StartupStatus {
    private static volatile String error;

    private StartupStatus() {
    }

    public static void fail(String safeMessage) {
        error = safeMessage;
    }

    public static void clear() {
        error = null;
    }

    public static String error() {
        return error;
    }
}
