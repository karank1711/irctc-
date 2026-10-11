<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ include file="fragments/layout-start.jsp" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<div class="page-head"><div><h1>Booking classes</h1><p class="muted">These labels appear on the class chart. The percentages are counted from bookings.</p></div><a href="${ctx}/manage">Back</a></div>
<c:choose>
    <c:when test="${empty classes}"><section class="card"><div class="empty"><p>No class configuration is stored yet.</p></div></section></c:when>
    <c:otherwise>
        <c:forEach var="item" items="${classes}">
            <section class="card" style="margin-bottom:12px;">
                <form method="post" action="${ctx}/manage/classes/save">
                    <input type="hidden" name="csrfToken" value="${csrfToken}">
                    <input type="hidden" name="code" value="${item.code}">
                    <div class="form-grid">
                        <div class="field"><label>Code</label><input value="${item.code}" disabled></div>
                        <div class="field"><label for="name-${item.code}">Display name</label><input id="name-${item.code}" name="displayName" required maxlength="80" value="${fn:escapeXml(item.name)}"></div>
                        <div class="field"><label for="color-${item.code}">Color</label><input id="color-${item.code}" name="color" required pattern="#[0-9A-Fa-f]{6}" value="${fn:escapeXml(item.color)}"></div>
                        <div class="field"><label for="order-${item.code}">Sort order</label><input id="order-${item.code}" name="sortOrder" type="number" min="1" max="20" required value="${item.sortOrder}"></div>
                    </div>
                    <label><input type="checkbox" name="visible" ${item.offered ? 'checked' : ''}> Show on the dashboard</label>
                    <div style="margin-top:12px;"><button class="primary" type="submit">Save ${item.code}</button></div>
                </form>
            </section>
        </c:forEach>
    </c:otherwise>
</c:choose>
<%@ include file="fragments/layout-end.jsp" %>
