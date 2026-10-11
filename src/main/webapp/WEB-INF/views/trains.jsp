<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ include file="fragments/layout-start.jsp" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<div class="page-head"><div><h1>Train Management</h1></div><span><a class="secondary" href="${ctx}/trains/schedule">Schedules</a> <a class="primary" href="${ctx}/trains/new">Add train</a></span></div>
<form class="filters" method="get" action="${ctx}/trains">
    <label class="filter-search">
        <i data-lucide="search"></i>
        <input name="q" value="${fn:escapeXml(query)}" placeholder="Search trains by number, name, or station" aria-label="Search trains">
    </label>
    <select name="status" aria-label="Train status"><option value="">All statuses</option><option value="ACTIVE" ${status == 'ACTIVE' ? 'selected' : ''}>Active</option><option value="INACTIVE" ${status == 'INACTIVE' ? 'selected' : ''}>Inactive</option></select>
    <button class="primary icon-filter" type="submit" title="Filter" aria-label="Filter"><i data-lucide="list-filter"></i></button>
    <c:if test="${not empty query or not empty status}"><a class="clear-filter" href="${ctx}/trains">Clear</a></c:if>
</form>
<section class="card">
    <c:choose>
        <c:when test="${result.withoutRows}"><div class="empty"><h3>No trains found</h3><p>Add a train or change the filter. Stations must exist before a train can be saved.</p></div></c:when>
        <c:otherwise>
            <div class="table-wrap"><table>
                <thead><tr><th>Number</th><th>Name</th><th>Route</th><th>Departure</th><th>Days</th><th>Status</th><th></th></tr></thead>
                <tbody>
                <c:forEach var="train" items="${result.items}">
                    <tr>
                        <td><a href="${ctx}/trains/view?id=${train.id}"><c:out value="${train.trainNumber}"/></a></td>
                        <td><c:out value="${train.trainName}"/></td>
                        <td><c:out value="${train.routeLabel}"/></td>
                        <td><c:out value="${train.departureLabel}"/></td>
                        <td><c:out value="${train.runningDays}"/></td>
                        <td><span class="badge badge-${train.status == 'ACTIVE' ? 'active' : 'inactive'}"><c:out value="${train.statusLabel}"/></span></td>
                        <td><a href="${ctx}/trains/edit?id=${train.id}">Edit</a></td>
                    </tr>
                </c:forEach>
                </tbody>
            </table></div>
            <%@ include file="fragments/pagination.jsp" %>
        </c:otherwise>
    </c:choose>
</section>
<%@ include file="fragments/layout-end.jsp" %>
