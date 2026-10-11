package com.irctc.exception;

/**
 * A database failure with a message that is safe to show in the browser.
 * The original SQL error is kept for the server log only.
 */
public class DataAccessException extends RuntimeException {
    public DataAccessException(String safeMessage, Throwable cause) {
        super(safeMessage, cause);
    }
}
