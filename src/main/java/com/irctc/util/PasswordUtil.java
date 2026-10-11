package com.irctc.util;

import org.mindrot.jbcrypt.BCrypt;

/** Hashes and checks passwords. Plain passwords are never stored. */
public final class PasswordUtil {
    private static final int STRENGTH = 12;

    private PasswordUtil() {
    }

    public static String hash(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(STRENGTH));
    }

    public static boolean matches(String plainPassword, String passwordHash) {
        if (plainPassword == null || plainPassword.isEmpty() || passwordHash == null || passwordHash.isBlank()) {
            return false;
        }
        if (plainPassword.length() > 72) {
            return false;
        }
        try {
            return BCrypt.checkpw(plainPassword, passwordHash);
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }
}
