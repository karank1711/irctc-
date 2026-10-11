<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ include file="fragments/layout-start.jsp" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<div class="page-head"><h1>Train schedule</h1></div>
<section class="card">
    <form class="filters" method="get" action="${ctx}/trains/schedule">
        <label for="trainId">Train</label>
        <select id="trainId" name="trainId" aria-label="Train">
            <option value="">Choose a train</option>
            <c:forEach var="train" items="${trains}"><option value="${train.id}" ${selectedTrain.id == train.id ? 'selected' : ''}><c:out value="${train.trainNumber}"/> · <c:out value="${train.trainName}"/></option></c:forEach>
        </select>
    </form>
    <c:if test="${empty trains}"><p class="muted">No trains are available yet.</p></c:if>
    <c:if test="${not empty selectedTrain}">
        <h2><c:out value="${selectedTrain.trainNumber}"/> stops</h2>
        <c:choose>
            <c:when test="${empty stops}"><p class="muted">This train has no stops yet.</p></c:when>
            <c:otherwise>
                <div class="table-wrap"><table>
                    <thead><tr><th>#</th><th>Station</th><th>Arrival</th><th>Departure</th><th>Day</th><th>Km</th><th></th></tr></thead>
                    <tbody>
                    <c:forEach var="stop" items="${stops}">
                        <tr>
                            <td><c:out value="${stop.stopOrder}"/></td>
                            <td><c:out value="${stop.stationCode}"/> · <c:out value="${stop.stationName}"/></td>
                            <td><c:out value="${stop.arrivalLabel}"/></td>
                            <td><c:out value="${stop.departureLabel}"/></td>
                            <td><c:out value="${stop.dayOffset}"/></td>
                            <td><c:out value="${stop.distanceLabel}"/></td>
                            <td>
                                <form method="post" action="${ctx}/trains/schedule/delete" data-confirm="Remove this stop?">
                                    <input type="hidden" name="csrfToken" value="${csrfToken}">
                                    <input type="hidden" name="trainId" value="${selectedTrain.id}">
                                    <input type="hidden" name="stopId" value="${stop.id}">
                                    <button class="danger" type="submit">Remove</button>
                                </form>
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table></div>
            </c:otherwise>
        </c:choose>
        <h2 style="margin-top:18px;">Add a stop</h2>
        <form method="post" action="${ctx}/trains/schedule/save">
            <input type="hidden" name="csrfToken" value="${csrfToken}">
            <input type="hidden" name="trainId" value="${selectedTrain.id}">
            <div class="form-grid">
                <div class="field"><label for="stationId">Station</label>
                    <select id="stationId" name="stationId" required>
                        <c:forEach var="station" items="${stations}"><option value="${station.id}"><c:out value="${station.label}"/></option></c:forEach>
                    </select><span class="error"><c:out value="${errors.stationId}"/></span></div>
                <div class="field"><label for="stopOrder">Stop order</label><input id="stopOrder" name="stopOrder" type="number" min="1" max="30" required value="${stopForm.stopOrder}"><span class="error"><c:out value="${errors.stopOrder}"/></span></div>
                <div class="field"><label for="arrivalTime">Arrival</label><input id="arrivalTime" name="arrivalTime" type="time" value="${stopForm.arrivalTime}"></div>
                <div class="field"><label for="departureTime">Departure</label><input id="departureTime" name="departureTime" type="time" value="${stopForm.departureTime}"></div>
                <div class="field"><label for="dayOffset">Day offset</label><input id="dayOffset" name="dayOffset" type="number" min="0" max="5" value="${empty stopForm ? 0 : stopForm.dayOffset}"><span class="error"><c:out value="${errors.dayOffset}"/></span></div>
                <div class="field"><label for="distanceKm">Distance km</label><input id="distanceKm" name="distanceKm" required value="${stopForm.distanceKm}"><span class="error"><c:out value="${errors.distanceKm}"/></span></div>
            </div>
            <button class="primary" type="submit">Add stop</button>
        </form>
    </c:if>
</section>
<%@ include file="fragments/layout-end.jsp" %>
