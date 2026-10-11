package com.irctc.controller;

import com.irctc.util.UploadStore;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Sends a previously uploaded banner image to a signed-in administrator. */
public class MediaServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String path = request.getPathInfo();
        String fileName = path == null || path.length() < 2 ? "" : path.substring(1);
        Path file = UploadStore.resolve(fileName);
        if (file == null || !Files.isRegularFile(file)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".png")) {
            response.setContentType("image/png");
        } else if (lower.endsWith(".webp")) {
            response.setContentType("image/webp");
        } else if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
            response.setContentType("image/jpeg");
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("Cache-Control", "private, max-age=3600");
        Files.copy(file, response.getOutputStream());
    }
}
