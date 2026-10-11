<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ include file="fragments/layout-start.jsp" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<div class="page-head"><h1><c:out value="${train.trainNumber}"/> · <c:out value="${train.trainName}"/></h1><a href="${ctx}/trains/schedule?trainId=${train.id}">Schedule</a></div>
<section class="card">
    <dl class="detail-list">
        <dt>Route</dt><dd><c:out value="${train.sourceName}"/> (<c:out value="${train.sourceCode}"/>) to <c:out value="${train.destinationName}"/> (<c:out value="${train.destinationCode}"/>)</dd>
        <dt>Departure</dt><dd><c:out value="${train.departureLabel}"/></dd>
        <dt>Arrival</dt><dd><c:out value="${train.arrivalLabel}"/></dd>
        <dt>Days</dt><dd><c:out value="${train.runningDays}"/></dd>
        <dt>Status</dt><dd><c:out value="${train.statusLabel}"/></dd>
    </dl>
    <p><a href="${ctx}/trains/edit?id=${train.id}">Edit train</a></p>
    <form method="post" action="${ctx}/trains/status" data-confirm="Change this train's status?">
        <input type="hidden" name="csrfToken" value="${csrfToken}">
        <input type="hidden" name="id" value="${train.id}">
        <input type="hidden" name="status" value="${train.status == 'ACTIVE' ? 'INACTIVE' : 'ACTIVE'}">
        <button class="${train.status == 'ACTIVE' ? 'danger' : 'primary'}" type="submit">${train.status == 'ACTIVE' ? 'Deactivate' : 'Activate'}</button>
    </form>
    <h2 style="margin-top:18px;">Stops</h2>
    <c:choose>
        <c:when test="${empty stops}"><p class="muted">No schedule stops yet.</p></c:when>
        <c:otherwise>
            <div class="table-wrap"><table>
                <thead><tr><th>#</th><th>Station</th><th>Arrival</th><th>Departure</th><th>Day</th><th>Km</th></tr></thead>
                <tbody>
                <c:forEach var="stop" items="${stops}">
                    <tr>
                        <td><c:out value="${stop.stopOrder}"/></td>
                        <td><c:out value="${stop.stationCode}"/> · <c:out value="${stop.stationName}"/></td>
                        <td><c:out value="${stop.arrivalLabel}"/></td>
                        <td><c:out value="${stop.departureLabel}"/></td>
                        <td><c:out value="${stop.dayOffset}"/></td>
                        <td><c:out value="${stop.distanceLabel}"/></td>
                    </tr>
                </c:forEach>
                </tbody>
            </table></div>
        </c:otherwise>
    </c:choose>
</section>
<%@ include file="fragments/layout-end.jsp" %>
