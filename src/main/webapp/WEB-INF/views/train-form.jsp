<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ include file="fragments/layout-start.jsp" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<div class="page-head"><h1><c:out value="${editing ? 'Edit train' : 'Add train'}"/></h1><a href="${ctx}/trains">Back</a></div>
<section class="card">
    <c:if test="${empty stations}"><p class="form-error">Add stations in the database before creating a train.</p></c:if>
    <c:if test="${not empty errors.form}"><p class="form-error"><c:out value="${errors.form}"/></p></c:if>
    <form method="post" action="${ctx}/trains/save">
        <input type="hidden" name="csrfToken" value="${csrfToken}">
        <input type="hidden" name="id" value="${form.id}">
        <div class="form-grid">
            <div class="field"><label for="trainNumber">Train number</label><input id="trainNumber" name="trainNumber" required pattern="[0-9]{4,5}" value="${fn:escapeXml(form.trainNumber)}"><span class="error"><c:out value="${errors.trainNumber}"/></span></div>
            <div class="field"><label for="trainName">Train name</label><input id="trainName" name="trainName" required maxlength="120" value="${fn:escapeXml(form.trainName)}"><span class="error"><c:out value="${errors.trainName}"/></span></div>
            <div class="field"><label for="sourceStationId">Origin</label>
                <select id="sourceStationId" name="sourceStationId" required>
                    <c:forEach var="station" items="${stations}"><option value="${station.id}" ${form.sourceStationId == station.id ? 'selected' : ''}><c:out value="${station.label}"/></option></c:forEach>
                </select><span class="error"><c:out value="${errors.sourceStationId}"/></span></div>
            <div class="field"><label for="destinationStationId">Destination</label>
                <select id="destinationStationId" name="destinationStationId" required>
                    <c:forEach var="station" items="${stations}"><option value="${station.id}" ${form.destinationStationId == station.id ? 'selected' : ''}><c:out value="${station.label}"/></option></c:forEach>
                </select><span class="error"><c:out value="${errors.destinationStationId}"/></span></div>
            <div class="field"><label for="departureTime">Departure</label><input id="departureTime" name="departureTime" type="time" required value="${form.departureTime}"><span class="error"><c:out value="${errors.departureTime}"/></span></div>
            <div class="field"><label for="arrivalTime">Arrival</label><input id="arrivalTime" name="arrivalTime" type="time" required value="${form.arrivalTime}"><span class="error"><c:out value="${errors.arrivalTime}"/></span><small class="muted">Arrival may be on a later day.</small></div>
            <div class="field"><label for="status">Status</label><select id="status" name="status"><option value="ACTIVE" ${form.status == 'ACTIVE' ? 'selected' : ''}>Active</option><option value="INACTIVE" ${form.status == 'INACTIVE' ? 'selected' : ''}>Inactive</option></select></div>
        </div>
        <div class="field"><span>Running days</span><div class="days">
            <c:forEach var="day" items="${days}"><label><input type="checkbox" name="runningDays" value="${day}" ${form.runsOn(day) ? 'checked' : ''}> <c:out value="${day}"/></label></c:forEach>
        </div><span class="error"><c:out value="${errors.runningDays}"/></span></div>
        <button class="primary" type="submit">Save train</button>
    </form>
</section>
<%@ include file="fragments/layout-end.jsp" %>
