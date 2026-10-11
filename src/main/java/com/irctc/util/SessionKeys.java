package com.irctc.util;

/** Names of values stored in the HTTP session. */
public final class SessionKeys {
    public static final String ADMIN_ID = "adminId";
    public static final String ADMIN_NAME = "adminName";
    public static final String ADMIN_EMAIL = "adminEmail";
    public static final String CSRF = "csrfToken";
    public static final String LOGIN_TIME = "loginTime";
    public static final String FLASH_SUCCESS = "flashSuccess";
    public static final String FLASH_ERROR = "flashError";

    private SessionKeys() {
    }
}
