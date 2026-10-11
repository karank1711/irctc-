package com.irctc.controller;

import com.irctc.dao.SearchDAO;
import com.irctc.exception.DataAccessException;
import com.irctc.model.SearchHit;
import com.irctc.util.JsonWriter;
import com.irctc.util.Web;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SearchController extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(SearchController.class.getName());
    private final SearchDAO searchDAO = new SearchDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String query = Web.param(request, "q");
        boolean api = "/api/search".equals(Web.path(request));
        if (query.length() < 2) {
            if (api) {
                JsonWriter.write(response, Map.of("results", List.of()));
                return;
            }
            request.setAttribute("query", query);
            request.setAttribute("results", List.of());
            request.setAttribute("hint", "Type at least 2 characters to search PNR, train, user, or employee records.");
            request.setAttribute("pageTitle", "Search");
            Web.forward(request, response, "search");
            return;
        }
        try {
            List<SearchHit> results = searchDAO.search(query, api ? 5 : 20);
            if (api) {
                JsonWriter.write(response, Map.of("results", results));
                return;
            }
            request.setAttribute("query", query);
            request.setAttribute("results", results);
            request.setAttribute("pageTitle", "Search");
            Web.forward(request, response, "search");
        } catch (DataAccessException ex) {
            LOGGER.log(Level.SEVERE, "Search failed", ex);
            if (api) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                JsonWriter.write(response, Map.of("error", "Search could not be completed."));
                return;
            }
            request.setAttribute("errorTitle", "Search unavailable");
            request.setAttribute("errorMessage", "Search could not be completed.");
            Web.forward(request, response, "error");
        }
    }
}
