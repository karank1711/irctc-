<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ include file="fragments/layout-start.jsp" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<div class="page-head"><h1>Search</h1></div>
<section class="card">
    <form class="filters" method="get" action="${ctx}/search">
        <label class="filter-search">
            <i data-lucide="search"></i>
            <input name="q" value="${fn:escapeXml(query)}" placeholder="Search employees, bookings, trains..." aria-label="Search">
        </label>
        <button class="primary icon-filter" type="submit" title="Search" aria-label="Search"><i data-lucide="search"></i></button>
    </form>
    <c:if test="${not empty hint}"><p class="muted"><c:out value="${hint}"/></p></c:if>
    <c:if test="${empty hint && empty results}"><div class="empty"><p>No matching records.</p></div></c:if>
    <c:forEach var="hit" items="${results}">
        <a class="search-hit" href="${ctx}${hit.url}">
            <span><strong><c:out value="${hit.title}"/></strong><br><small><c:out value="${hit.type}"/> · <c:out value="${hit.subtitle}"/></small></span>
        </a>
    </c:forEach>
</section>
<%@ include file="fragments/layout-end.jsp" %>
