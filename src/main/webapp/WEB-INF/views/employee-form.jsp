<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ include file="fragments/layout-start.jsp" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<div class="page-head"><h1><c:out value="${editing ? 'Edit employee' : 'Add employee'}"/></h1><a href="${ctx}/employees">Back</a></div>
<section class="card">
    <c:if test="${not empty errors.form}"><p class="form-error"><c:out value="${errors.form}"/></p></c:if>
    <form method="post" action="${ctx}/employees/save">
        <input type="hidden" name="csrfToken" value="${csrfToken}">
        <input type="hidden" name="id" value="${form.id}">
        <c:if test="${editing}"><p class="muted">Employee code <c:out value="${form.employeeCode}"/></p></c:if>
        <div class="form-grid">
            <div class="field"><label for="fullName">Full name</label><input id="fullName" name="fullName" required maxlength="120" value="${fn:escapeXml(form.fullName)}"><span class="error"><c:out value="${errors.fullName}"/></span></div>
            <div class="field"><label for="email">Email</label><input id="email" name="email" type="email" required maxlength="150" value="${fn:escapeXml(form.email)}"><span class="error"><c:out value="${errors.email}"/></span></div>
            <div class="field"><label for="phone">Mobile</label><input id="phone" name="phone" required pattern="[6-9][0-9]{9}" value="${fn:escapeXml(form.phone)}"><span class="error"><c:out value="${errors.phone}"/></span></div>
            <div class="field"><label for="department">Department</label><input id="department" name="department" required maxlength="80" value="${fn:escapeXml(form.department)}"><span class="error"><c:out value="${errors.department}"/></span></div>
            <div class="field"><label for="designation">Designation</label><input id="designation" name="designation" required maxlength="80" value="${fn:escapeXml(form.designation)}"><span class="error"><c:out value="${errors.designation}"/></span></div>
            <div class="field"><label for="status">Status</label><select id="status" name="status"><option value="ACTIVE" ${form.status == 'ACTIVE' ? 'selected' : ''}>Active</option><option value="INACTIVE" ${form.status == 'INACTIVE' ? 'selected' : ''}>Inactive</option></select></div>
        </div>
        <button class="primary" type="submit">Save employee</button>
    </form>
</section>
<%@ include file="fragments/layout-end.jsp" %>
