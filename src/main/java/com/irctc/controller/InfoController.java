package com.irctc.controller;

import com.irctc.util.Web;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class InfoController extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String path = Web.path(request);
        String title = switch (path) {
            case "/privacy" -> "Privacy";
            case "/terms" -> "Terms";
            case "/help" -> "Help";
            default -> null;
        };
        if (title == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        request.setAttribute("infoPage", path.substring(1));
        request.setAttribute("pageTitle", title);
        Web.forward(request, response, "info");
    }
}
