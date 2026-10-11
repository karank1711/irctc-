<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ include file="fragments/layout-start.jsp" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<div class="page-head"><div><h1>Routes</h1><p class="muted">Booking counts are grouped from stored tickets. City names come from the station records.</p></div><a href="${ctx}/manage">Back</a></div>
<section class="card">
    <c:choose>
        <c:when test="${empty routes}"><div class="empty"><p>No routes yet. A route appears after a booking is stored.</p></div></c:when>
        <c:otherwise>
            <div class="table-wrap">
                <table>
                    <thead><tr><th>Route</th><th>Stations</th><th>Bookings</th></tr></thead>
                    <tbody>
                    <c:forEach var="route" items="${routes}">
                        <tr>
                            <td><c:out value="${route.routeLabel}"/></td>
                            <td><c:out value="${route.source}"/> → <c:out value="${route.destination}"/></td>
                            <td><c:out value="${route.bookingsLabel}"/></td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>
        </c:otherwise>
    </c:choose>
</section>
<section class="card" style="margin-top:16px; max-width:720px;">
    <h2 style="margin-bottom:12px;">Station display name</h2>
    <form method="post" action="${ctx}/manage/routes/station">
        <input type="hidden" name="csrfToken" value="${csrfToken}">
        <div class="field"><label for="stationId">Station</label>
            <select id="stationId" name="stationId" required>
                <option value="">Choose a station</option>
                <c:forEach var="station" items="${stations}">
                    <option value="${station.id}" ${stationForm.id == station.id ? 'selected' : ''}><c:out value="${station.stationCode}"/> · <c:out value="${station.city}"/></option>
                </c:forEach>
            </select>
            <span class="error"><c:out value="${errors.stationId}"/></span>
        </div>
        <div class="form-grid">
            <div class="field"><label for="stationName">Station name</label><input id="stationName" name="stationName" required maxlength="120" value="${fn:escapeXml(stationForm.stationName)}"><span class="error"><c:out value="${errors.stationName}"/></span></div>
            <div class="field"><label for="city">City shown on routes</label><input id="city" name="city" required maxlength="80" value="${fn:escapeXml(stationForm.city)}"><span class="error"><c:out value="${errors.city}"/></span></div>
            <div class="field"><label for="stateName">State</label><input id="stateName" name="stateName" required maxlength="80" value="${fn:escapeXml(stationForm.stateName)}"><span class="error"><c:out value="${errors.stateName}"/></span></div>
        </div>
        <button class="primary" type="submit">Save station</button>
    </form>
    <p class="muted" style="margin-top:12px;">Pick a station from the list below to fill this form.</p>
    <div class="table-wrap">
        <table>
            <thead><tr><th>Code</th><th>Name</th><th>City</th><th></th></tr></thead>
            <tbody>
            <c:forEach var="station" items="${stations}">
                <tr>
                    <td><c:out value="${station.stationCode}"/></td>
                    <td><c:out value="${station.stationName}"/></td>
                    <td><c:out value="${station.city}"/></td>
                    <td><a href="${ctx}/manage/routes?stationId=${station.id}">Edit</a></td>
                </tr>
            </c:forEach>
            </tbody>
        </table>
    </div>
</section>
<%@ include file="fragments/layout-end.jsp" %>
