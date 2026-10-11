<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="My Profile" scope="request"/>
<%@ include file="fragments/layout-start.jsp" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<div class="page-head"><h1>My Profile</h1></div>
<section class="card" style="max-width:640px;">
    <dl class="detail-list">
        <dt>Email</dt><dd><c:out value="${admin.email}"/></dd>
        <dt>Last sign-in</dt><dd><c:out value="${lastLoginLabel}"/></dd>
    </dl>
    <form method="post" action="${ctx}/profile" style="margin-top:18px;">
        <input type="hidden" name="csrfToken" value="${csrfToken}">
        <div class="field">
            <label for="fullName">Display name</label>
            <input id="fullName" name="fullName" required maxlength="100" value="${fn:escapeXml(fullNameInput)}">
            <span class="error"><c:out value="${errors.fullName}"/></span>
        </div>
        <button class="primary" type="submit">Save profile</button>
        <a class="secondary" href="${ctx}/settings">Password and portal settings</a>
    </form>
</section>
<%@ include file="fragments/layout-end.jsp" %>
