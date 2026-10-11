package com.irctc.util;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public final class JsonWriter {
    private static final Gson GSON = new Gson();

    private JsonWriter() {
    }

    public static String stringify(Object value) {
        return GSON.toJson(value);
    }

    public static void write(HttpServletResponse response, Object value) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        GSON.toJson(value, response.getWriter());
    }
}
