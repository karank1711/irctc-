<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ include file="fragments/layout-start.jsp" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<div class="page-head"><div><h1>Bookings</h1><p class="muted">Records stored in the database.</p></div><a class="primary" href="${ctx}/bookings/new">Record booking</a></div>
<form class="filters" method="get" action="${ctx}/bookings">
    <label class="filter-search">
        <i data-lucide="search"></i>
        <input name="q" value="${fn:escapeXml(query)}" placeholder="Search bookings by PNR, train, or passenger" aria-label="Search bookings">
    </label>
    <select name="status" aria-label="Booking status">
        <option value="">All statuses</option>
        <c:forEach var="item" items="${['CONFIRMED','WAITING','RAC','CANCELLED']}">
            <option value="${item}" ${status == item ? 'selected' : ''}><c:out value="${item}"/></option>
        </c:forEach>
    </select>
    <button class="primary icon-filter" type="submit" title="Filter" aria-label="Filter"><i data-lucide="list-filter"></i></button>
    <c:if test="${not empty query or not empty status}"><a class="clear-filter" href="${ctx}/bookings">Clear</a></c:if>
</form>
<section class="card">
    <c:choose>
        <c:when test="${result.withoutRows}"><div class="empty"><h3>No bookings found</h3><p>Try another search, or record a booking.</p></div></c:when>
        <c:otherwise>
            <div class="table-wrap"><table>
                <thead><tr><th>PNR</th><th>Train Number</th><th>From → To</th><th>Booking Time</th><th>Status</th></tr></thead>
                <tbody>
                <c:forEach var="booking" items="${result.items}">
                    <tr>
                        <td><a href="${ctx}/bookings/view?id=${booking.id}"><c:out value="${booking.pnr}"/></a></td>
                        <td><c:out value="${booking.trainNumber}"/></td>
                        <td><c:out value="${booking.routeLabel}"/></td>
                        <td><c:out value="${booking.bookingTimeLabel}"/></td>
                        <td><span class="badge badge-${booking.statusTone}"><c:out value="${booking.statusLabel}"/></span></td>
                    </tr>
                </c:forEach>
                </tbody>
            </table></div>
            <%@ include file="fragments/pagination.jsp" %>
        </c:otherwise>
    </c:choose>
</section>
<%@ include file="fragments/layout-end.jsp" %>
