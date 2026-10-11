<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ include file="fragments/layout-start.jsp" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<div class="page-head"><h1><c:out value="${employee.fullName}"/></h1><a href="${ctx}/employees/edit?id=${employee.id}">Edit</a></div>
<section class="card">
    <dl class="detail-list">
        <dt>Code</dt><dd><c:out value="${employee.employeeCode}"/></dd>
        <dt>Email</dt><dd><c:out value="${employee.email}"/></dd>
        <dt>Phone</dt><dd><c:out value="${employee.phone}"/></dd>
        <dt>Department</dt><dd><c:out value="${employee.department}"/></dd>
        <dt>Designation</dt><dd><c:out value="${employee.designation}"/></dd>
        <dt>Status</dt><dd><c:out value="${employee.statusLabel}"/></dd>
        <dt>Added</dt><dd><c:out value="${employee.createdLabel}"/></dd>
    </dl>
    <form method="post" action="${ctx}/employees/status" data-confirm="Change this employee's status?">
        <input type="hidden" name="csrfToken" value="${csrfToken}">
        <input type="hidden" name="id" value="${employee.id}">
        <input type="hidden" name="status" value="${employee.status == 'ACTIVE' ? 'INACTIVE' : 'ACTIVE'}">
        <button class="${employee.status == 'ACTIVE' ? 'danger' : 'primary'}" type="submit">${employee.status == 'ACTIVE' ? 'Deactivate' : 'Activate'}</button>
    </form>
</section>
<%@ include file="fragments/layout-end.jsp" %>
