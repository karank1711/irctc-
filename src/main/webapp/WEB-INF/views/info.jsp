<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ include file="fragments/layout-start.jsp" %>
<section class="card">
    <c:choose>
        <c:when test="${infoPage == 'privacy'}">
            <h1>Privacy</h1>
            <p>This administration portal keeps administrator sessions and the records you enter in the MySQL database configured for this application. It does not connect to live IRCTC customer systems.</p>
            <p>Passwords are stored as BCrypt hashes. Do not share the development .env file.</p>
        </c:when>
        <c:when test="${infoPage == 'terms'}">
            <h1>Terms</h1>
            <p>This portal is a local administration tool for learning and development. Sample bookings, users, employees, and trains are fictional. They must not be presented as official IRCTC production data.</p>
        </c:when>
        <c:otherwise>
            <h1>Help</h1>
            <p>Sign in with the administrator email and password from the server .env file. The password in that file is used only to create the account the first time.</p>
            <p>Dashboard totals, charts, search, and tables are read from MySQL. If a comparison says “Comparison unavailable”, the previous period has no rows to compare.</p>
            <p>Use Settings to change the stored administrator password before any shared deployment.</p>
        </c:otherwise>
    </c:choose>
</section>
<%@ include file="fragments/layout-end.jsp" %>
