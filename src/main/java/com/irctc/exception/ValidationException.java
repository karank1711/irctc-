package com.irctc.exception;

import java.util.Collections;
import java.util.Map;

/** Form input failed server-side validation. */
public class ValidationException extends Exception {
    private final Map<String, String> fieldErrors;

    public ValidationException(Map<String, String> fieldErrors) {
        super("Please correct the highlighted fields.");
        this.fieldErrors = Collections.unmodifiableMap(fieldErrors);
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }
}
