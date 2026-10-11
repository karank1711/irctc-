<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ include file="fragments/layout-start.jsp" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<div class="page-head"><div><h1>Activity</h1><p class="muted">Entries are written when an administrator takes an action. Seeded rows are part of the development sample.</p></div></div>
<form class="filters" method="get" action="${ctx}/activities">
    <select name="type" aria-label="Activity type">
        <option value="">All types</option>
        <c:forEach var="item" items="${types}"><option value="${item.activityType}" ${type == item.activityType ? 'selected' : ''}><c:out value="${item.typeLabel}"/></option></c:forEach>
    </select>
    <button class="primary icon-filter" type="submit" title="Filter" aria-label="Filter"><i data-lucide="list-filter"></i></button>
    <c:if test="${not empty type}"><a class="clear-filter" href="${ctx}/activities">Clear</a></c:if>
</form>
<section class="card">
    <c:choose>
        <c:when test="${result.withoutRows}"><div class="empty"><h3>No activity yet</h3><p>Creating a booking, user, employee, train, or report will add an entry. Clear the filter to see every type.</p></div></c:when>
        <c:otherwise>
            <c:forEach var="item" items="${result.items}">
                <div class="activity-item">
                    <span class="activity-icon"><i data-lucide="${item.icon}"></i></span>
                    <span class="activity-copy"><p><c:out value="${item.message}"/></p><small class="muted"><c:out value="${item.typeLabel}"/></small></span>
                    <time class="activity-time"><c:out value="${item.exactTimeLabel}"/></time>
                </div>
            </c:forEach>
            <%@ include file="fragments/pagination.jsp" %>
        </c:otherwise>
    </c:choose>
</section>
<%@ include file="fragments/layout-end.jsp" %>
