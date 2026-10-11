<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<nav class="pager" aria-label="Pagination">
    <c:forEach var="link" items="${links}">
        <c:choose>
            <c:when test="${link.disabled}"><span class="disabled"><c:out value="${link.label}"/></span></c:when>
            <c:when test="${link.current}"><span class="current"><c:out value="${link.label}"/></span></c:when>
            <c:otherwise><a href="${pageContext.request.contextPath}${link.url}"><c:out value="${link.label}"/></a></c:otherwise>
        </c:choose>
    </c:forEach>
</nav>
