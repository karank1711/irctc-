<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ include file="fragments/layout-start.jsp" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<div class="page-head"><h1><c:out value="${user.fullName}"/></h1><a href="${ctx}/users/edit?id=${user.id}">Edit</a></div>
<section class="card">
    <dl class="detail-list">
        <dt>Email</dt><dd><c:out value="${user.email}"/></dd>
        <dt>Phone</dt><dd><c:out value="${user.phone}"/></dd>
        <dt>Status</dt><dd><c:out value="${user.statusLabel}"/></dd>
        <dt>Registered</dt><dd><c:out value="${user.createdLabel}"/></dd>
    </dl>
    <form method="post" action="${ctx}/users/status" data-confirm="Change this user's status?">
        <input type="hidden" name="csrfToken" value="${csrfToken}">
        <input type="hidden" name="id" value="${user.id}">
        <input type="hidden" name="status" value="${user.status == 'ACTIVE' ? 'INACTIVE' : 'ACTIVE'}">
        <button class="${user.status == 'ACTIVE' ? 'danger' : 'primary'}" type="submit">${user.status == 'ACTIVE' ? 'Deactivate' : 'Activate'}</button>
    </form>
</section>
<%@ include file="fragments/layout-end.jsp" %>
