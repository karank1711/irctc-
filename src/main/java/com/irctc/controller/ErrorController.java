package com.irctc.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class ErrorController extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Object status = request.getAttribute("jakarta.servlet.error.status_code");
        if (request.getAttribute("errorTitle") == null) {
            request.setAttribute("errorTitle", status == null ? "Something went wrong" : "Page not available");
        }
        if (request.getAttribute("errorMessage") == null) {
            request.setAttribute("errorMessage", "That page is not available, or the last action could not be completed.");
        }
        request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
    }
}
