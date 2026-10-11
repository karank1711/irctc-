<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ include file="fragments/layout-start.jsp" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<div class="page-head"><div><h1>PNR <c:out value="${booking.pnr}"/></h1><p class="muted"><c:out value="${booking.trainName}"/></p></div><a href="${ctx}/bookings">Back</a></div>
<section class="card">
    <dl class="detail-list">
        <dt>Train</dt><dd><c:out value="${booking.trainNumber}"/> · <c:out value="${booking.trainName}"/></dd>
        <dt>Passenger</dt><dd><c:out value="${booking.passengerName}"/></dd>
        <dt>Route</dt><dd><c:out value="${booking.routeLabel}"/></dd>
        <dt>Class</dt><dd><c:out value="${booking.travelClass}"/></dd>
        <dt>Service</dt><dd><c:out value="${booking.serviceName}"/></dd>
        <dt>Booking time</dt><dd><c:out value="${booking.bookingTimeLabel}"/></dd>
        <dt>Travel date</dt><dd><c:out value="${booking.travelDateLabel}"/></dd>
        <dt>Status</dt><dd><span class="badge badge-${booking.statusTone}"><c:out value="${booking.statusLabel}"/></span></dd>
        <dt>Amount</dt><dd><c:out value="${booking.amountLabel}"/></dd>
    </dl>
    <c:if test="${booking.bookingStatus != 'CANCELLED'}">
        <form method="post" action="${ctx}/bookings/cancel" data-confirm="Cancel this booking? This cannot be undone from the dashboard.">
            <input type="hidden" name="csrfToken" value="${csrfToken}">
            <input type="hidden" name="id" value="${booking.id}">
            <button class="danger" type="submit">Cancel booking</button>
        </form>
    </c:if>
</section>
<%@ include file="fragments/layout-end.jsp" %>
