package com.irctc.filter;

import com.irctc.service.LayoutService;
import com.irctc.util.JsonWriter;
import com.irctc.util.SessionKeys;
import com.irctc.util.Web;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Map;

/** Checks the session, blocks forged form posts, and prepares the shared page header. */
public class RequestGuardFilter implements Filter {
    private final LayoutService layoutService = new LayoutService();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        String path = Web.path(request);
        if (isAsset(path)) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = request.getSession(false);
        Web.moveFlash(request, session);
        boolean api = path.startsWith("/api/");
        boolean login = "/login".equals(path);
        boolean loggedIn = session != null && session.getAttribute(SessionKeys.ADMIN_ID) != null;

        if (login && "GET".equalsIgnoreCase(request.getMethod())) {
            if (loggedIn) {
                Web.redirect(request, response, "/dashboard");
                return;
            }
            ensureToken(request.getSession(true));
            request.setAttribute("csrfToken", request.getSession(false).getAttribute(SessionKeys.CSRF));
            chain.doFilter(request, response);
            return;
        }

        if (!loggedIn && !login) {
            if (api) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                JsonWriter.write(response, Map.of("error", "Sign in required."));
                return;
            }
            Web.redirect(request, response, "/login");
            return;
        }

        if ("POST".equalsIgnoreCase(request.getMethod())) {
            String expected = session == null ? null : (String) session.getAttribute(SessionKeys.CSRF);
            String actual = request.getParameter("csrfToken");
            if (actual == null || actual.isBlank()) {
                actual = request.getHeader("X-CSRF-Token");
            }
            if (!Web.sameToken(expected, actual)) {
                if (api) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    JsonWriter.write(response, Map.of("error", "The form token was missing or expired."));
                    return;
                }
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                request.setAttribute("errorTitle", "Request blocked");
                request.setAttribute("errorMessage", "The form token was missing or expired. Go back and try again.");
                request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
                return;
            }
        }

        if (loggedIn) {
            response.setHeader("Cache-Control", "no-store");
            layoutService.populate(request);
        } else if (login && session != null) {
            request.setAttribute("csrfToken", session.getAttribute(SessionKeys.CSRF));
        }
        chain.doFilter(request, response);
    }

    private void ensureToken(HttpSession session) {
        if (session.getAttribute(SessionKeys.CSRF) == null) {
            session.setAttribute(SessionKeys.CSRF, Web.newToken());
        }
        // The login page reads the token from the session through the request.
    }

    private boolean isAsset(String path) {
        return path.startsWith("/assets/") || "/favicon.ico".equals(path) || "/favicon.svg".equals(path);
    }
}
