<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ include file="fragments/layout-start.jsp" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<div class="page-head"><h1>Reports</h1></div>
<section class="card">
    <form class="filters" method="get" action="${ctx}/reports">
        <label for="from">From</label>
        <input id="from" name="from" type="date" required value="${fn:escapeXml(from)}">
        <label for="to">To</label>
        <input id="to" name="to" type="date" required value="${fn:escapeXml(to)}">
        <button class="primary icon-filter" type="submit" title="Apply date range" aria-label="Apply date range"><i data-lucide="list-filter"></i></button>
        <a class="clear-filter" href="${ctx}/reports">Clear</a>
    </form>
    <c:if test="${not empty errors.from}"><p class="form-error"><c:out value="${errors.from}"/></p></c:if>
    <c:if test="${not empty errors.to}"><p class="form-error"><c:out value="${errors.to}"/></p></c:if>
    <c:if test="${not empty summary}">
        <div class="kpi-grid">
            <article class="card"><p class="metric-label">Bookings</p><p class="metric-value"><c:out value="${summary.totalBookings}"/></p></article>
            <article class="card"><p class="metric-label">Confirmed</p><p class="metric-value"><c:out value="${summary.confirmed}"/></p></article>
            <article class="card"><p class="metric-label">Waiting / RAC</p><p class="metric-value"><c:out value="${summary.waiting + summary.rac}"/></p></article>
            <article class="card"><p class="metric-label">Revenue</p><p class="metric-value"><c:out value="${summary.revenueLabel}"/></p></article>
        </div>
        <form method="post" action="${ctx}/reports/export" style="margin:16px 0;">
            <input type="hidden" name="csrfToken" value="${csrfToken}">
            <input type="hidden" name="from" value="${fn:escapeXml(from)}">
            <input type="hidden" name="to" value="${fn:escapeXml(to)}">
            <button class="primary" type="submit">Export CSV</button>
        </form>
        <c:if test="${truncated}"><p class="sample-note">The table shows the newest 5000 rows. The totals above include the full date range.</p></c:if>
        <h2>Routes</h2>
        <c:choose>
            <c:when test="${empty summary.routes}"><p class="muted">No routes in this range.</p></c:when>
            <c:otherwise>
                <div class="table-wrap"><table>
                    <thead><tr><th>Route</th><th>Bookings</th></tr></thead>
                    <tbody>
                    <c:forEach var="route" items="${summary.routes}"><tr><td><c:out value="${route.routeLabel}"/></td><td><c:out value="${route.bookingsLabel}"/></td></tr></c:forEach>
                    </tbody>
                </table></div>
            </c:otherwise>
        </c:choose>
        <h2 style="margin-top:16px;">Bookings in range</h2>
        <c:choose>
            <c:when test="${empty summary.bookings}"><p class="muted">No bookings in this range.</p></c:when>
            <c:otherwise>
                <div class="table-wrap"><table>
                    <thead><tr><th>PNR</th><th>Train</th><th>Route</th><th>Status</th><th>Amount</th></tr></thead>
                    <tbody>
                    <c:forEach var="booking" items="${summary.bookings}">
                        <tr>
                            <td><a href="${ctx}/bookings/view?id=${booking.id}"><c:out value="${booking.pnr}"/></a></td>
                            <td><c:out value="${booking.trainNumber}"/></td>
                            <td><c:out value="${booking.routeLabel}"/></td>
                            <td><c:out value="${booking.statusLabel}"/></td>
                            <td><c:out value="${booking.amountLabel}"/></td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table></div>
            </c:otherwise>
        </c:choose>
    </c:if>
</section>
<%@ include file="fragments/layout-end.jsp" %>
