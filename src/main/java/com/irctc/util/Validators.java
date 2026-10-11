package com.irctc.util;

import com.irctc.exception.ValidationException;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/** Collects field errors for a form. Call {@link #finish()} when every check is done. */
public final class Validators {
    private final Map<String, String> errors = new LinkedHashMap<>();

    public void required(String field, String value, String label) {
        if (value == null || value.isBlank()) {
            errors.putIfAbsent(field, label + " is required.");
        }
    }

    public void maxLength(String field, String value, int max, String label) {
        if (value != null && value.length() > max) {
            errors.putIfAbsent(field, label + " must be " + max + " characters or fewer.");
        }
    }

    public void email(String field, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        if (!value.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            errors.putIfAbsent(field, "Enter a valid email address.");
        }
    }

    public void phone(String field, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        if (!value.matches("^[6-9][0-9]{9}$")) {
            errors.putIfAbsent(field, "Enter a 10-digit mobile number starting with 6, 7, 8, or 9.");
        }
    }

    public void oneOf(String field, String value, Set<String> allowed, String message) {
        if (value == null || !allowed.contains(value)) {
            errors.putIfAbsent(field, message);
        }
    }

    public void money(String field, BigDecimal amount, String label) {
        if (amount == null) {
            errors.putIfAbsent(field, label + " must be a number with up to 2 decimal places.");
            return;
        }
        if (amount.compareTo(new BigDecimal("1.00")) < 0 || amount.compareTo(new BigDecimal("200000.00")) > 0) {
            errors.putIfAbsent(field, label + " must be between 1.00 and 200000.00.");
        }
    }

    public void add(String field, String message) {
        errors.putIfAbsent(field, message);
    }

    public boolean isValid() {
        return errors.isEmpty();
    }

    public Map<String, String> getErrors() {
        return errors;
    }

    public void finish() throws ValidationException {
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }
}
