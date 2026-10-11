package com.irctc.controller;

import com.irctc.dao.DashboardDAO;
import com.irctc.exception.DataAccessException;
import com.irctc.service.DashboardService;
import com.irctc.util.Web;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServiceController extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(ServiceController.class.getName());
    private final DashboardService dashboardService = new DashboardService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            request.setAttribute("services", dashboardService.services());
            request.setAttribute("pageTitle", "Services");
            Web.forward(request, response, "services");
        } catch (DataAccessException ex) {
            LOGGER.log(Level.SEVERE, "Services could not be loaded", ex);
            request.setAttribute("errorTitle", "Services unavailable");
            request.setAttribute("errorMessage", "Services could not be loaded.");
            Web.forward(request, response, "error");
        }
    }
}
