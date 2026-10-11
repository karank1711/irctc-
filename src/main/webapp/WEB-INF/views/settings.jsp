<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ include file="fragments/layout-start.jsp" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<div class="page-head"><div><h1>Settings</h1><p class="muted">Preferences for this administration portal.</p></div></div>
<section class="split">
    <article class="card">
        <h2>Portal preferences</h2>
        <form method="post" action="${ctx}/settings">
            <input type="hidden" name="csrfToken" value="${csrfToken}">
            <input type="hidden" name="action" value="preferences">
            <div class="field"><label for="portalName">Portal name</label><input id="portalName" name="portalName" required maxlength="80" value="${fn:escapeXml(portalNameInput)}"><span class="error"><c:out value="${errors.portalName}"/></span></div>
            <div class="field"><label for="supportEmail">Support email</label><input id="supportEmail" name="supportEmail" type="email" required maxlength="150" value="${fn:escapeXml(supportEmailInput)}"><span class="error"><c:out value="${errors.supportEmail}"/></span></div>
            <div class="field"><label for="pageSize">Rows per page</label>
                <select id="pageSize" name="pageSize">
                    <option value="5" ${pageSizeInput == '5' ? 'selected' : ''}>5</option>
                    <option value="10" ${pageSizeInput == '10' ? 'selected' : ''}>10</option>
                    <option value="20" ${pageSizeInput == '20' ? 'selected' : ''}>20</option>
                </select><span class="error"><c:out value="${errors.pageSize}"/></span>
            </div>
            <button class="primary" type="submit">Save preferences</button>
        </form>
    </article>
    <article class="card">
        <h2>Change password</h2>
        <p class="muted">The password in the .env file is used only when the administrator account is first created. Changing it here updates the stored hash.</p>
        <form method="post" action="${ctx}/settings">
            <input type="hidden" name="csrfToken" value="${csrfToken}">
            <input type="hidden" name="action" value="password">
            <div class="field"><label for="currentPassword">Current password</label><input id="currentPassword" name="currentPassword" type="password" required autocomplete="current-password"><span class="error"><c:out value="${errors.currentPassword}"/></span></div>
            <div class="field"><label for="newPassword">New password</label><input id="newPassword" name="newPassword" type="password" required minlength="8" maxlength="72" autocomplete="new-password"><span class="error"><c:out value="${errors.newPassword}"/></span></div>
            <div class="field"><label for="confirmPassword">Confirm password</label><input id="confirmPassword" name="confirmPassword" type="password" required minlength="8" maxlength="72" autocomplete="new-password"><span class="error"><c:out value="${errors.confirmPassword}"/></span></div>
            <button class="primary" type="submit">Update password</button>
        </form>
    </article>
</section>
<%@ include file="fragments/layout-end.jsp" %>
