package com.irctc.util;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;

/** Small helpers shared by the controllers. */
public final class Web {
    private static final SecureRandom RANDOM = new SecureRandom();

    private Web() {
    }

    public static String path(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String context = request.getContextPath();
        String path = uri.startsWith(context) ? uri.substring(context.length()) : uri;
        int semicolon = path.indexOf(';');
        if (semicolon >= 0) {
            path = path.substring(0, semicolon);
        }
        if (path.length() > 1 && path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        return path.isEmpty() ? "/" : path;
    }

    public static String param(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        return value == null ? "" : value.trim();
    }

    public static int intParam(HttpServletRequest request, String name, int fallback) {
        try {
            return Integer.parseInt(param(request, name));
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    public static Long longParam(HttpServletRequest request, String name) {
        String value = param(request, name);
        if (value.isEmpty()) {
            return null;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    public static BigDecimal money(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            BigDecimal value = new BigDecimal(raw.trim());
            if (value.scale() > 2) {
                return null;
            }
            return value.setScale(2);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    public static String newToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public static boolean sameToken(String expected, String actual) {
        if (expected == null || actual == null) {
            return false;
        }
        return MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                actual.getBytes(StandardCharsets.UTF_8));
    }

    public static void forward(HttpServletRequest request, HttpServletResponse response, String view)
            throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/views/" + view + ".jsp").forward(request, response);
    }

    public static void redirect(HttpServletRequest request, HttpServletResponse response, String path) throws IOException {
        response.sendRedirect(request.getContextPath() + path);
    }

    public static void flash(HttpServletRequest request, String key, String message) {
        request.getSession().setAttribute(key, message);
    }

    public static long adminId(HttpServletRequest request) {
        return (Long) request.getSession().getAttribute(SessionKeys.ADMIN_ID);
    }

    public static String query(Map<String, String> params) {
        StringBuilder builder = new StringBuilder();
        params.forEach((key, value) -> {
            if (value == null || value.isBlank()) {
                return;
            }
            if (builder.length() > 0) {
                builder.append('&');
            }
            builder.append(URLEncoder.encode(key, StandardCharsets.UTF_8));
            builder.append('=');
            builder.append(URLEncoder.encode(value, StandardCharsets.UTF_8));
        });
        return builder.toString();
    }

    public static String safeReturnPath(String candidate) {
        if (candidate == null || !candidate.startsWith("/") || candidate.startsWith("//") || candidate.contains("://")) {
            return "/dashboard";
        }
        return candidate;
    }

    public static void moveFlash(HttpServletRequest request, HttpSession session) {
        if (session == null) {
            return;
        }
        moveOne(request, session, SessionKeys.FLASH_SUCCESS);
        moveOne(request, session, SessionKeys.FLASH_ERROR);
    }

    private static void moveOne(HttpServletRequest request, HttpSession session, String key) {
        Object value = session.getAttribute(key);
        if (value != null) {
            request.setAttribute(key, value);
            session.removeAttribute(key);
        }
    }
}
