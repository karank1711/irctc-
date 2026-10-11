<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ include file="fragments/layout-start.jsp" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<div class="page-head"><div><h1>Bookings &amp; statistics</h1><p class="muted">These figures are calculated from passengers, trains, and bookings. They are not typed in by hand.</p></div><a href="${ctx}/manage">Back</a></div>
<section class="kpi-grid">
    <c:forEach var="metric" items="${metrics}">
        <article class="card metric">
            <p class="metric-label"><c:out value="${metric.label}"/></p>
            <p class="metric-value"><c:out value="${metric.displayValue}"/></p>
            <p class="muted"><c:out value="${metric.comparisonAvailable ? metric.changeLabel : 'Comparison unavailable'}"/></p>
        </article>
    </c:forEach>
</section>
<p class="muted" style="margin-top:14px;">Change the underlying records from Bookings, Passengers, Trains, or Reports. The dashboard reloads these totals from MySQL.</p>
<%@ include file="fragments/layout-end.jsp" %>
