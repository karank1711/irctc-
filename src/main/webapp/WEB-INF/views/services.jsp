<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ include file="fragments/layout-start.jsp" %>
<div class="page-head"><div><h1>Services</h1><p class="muted">Categories are calculated from booking records. Services that are not offered stay visible so they are not mistaken for real totals.</p></div></div>
<section class="kpi-grid">
    <c:forEach var="service" items="${services}">
        <article class="card">
            <h2><c:out value="${service.name}"/></h2>
            <p class="metric-value"><c:out value="${service.countLabel}"/></p>
            <p class="muted"><c:out value="${service.description}"/></p>
            <p style="margin-top:10px;">
                <c:choose>
                    <c:when test="${service.offered}"><span class="badge badge-active">Offered · <c:out value="${service.percentLabel}"/></span></c:when>
                    <c:otherwise><span class="badge badge-inactive">Not offered in this module</span></c:otherwise>
                </c:choose>
            </p>
        </article>
    </c:forEach>
</section>
<c:if test="${empty services}"><section class="card empty"><p>No service categories are configured.</p></section></c:if>
<%@ include file="fragments/layout-end.jsp" %>
