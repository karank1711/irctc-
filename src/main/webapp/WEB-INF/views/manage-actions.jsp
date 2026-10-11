<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ include file="fragments/layout-start.jsp" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<div class="page-head"><div><h1>Quick actions</h1><p class="muted">Each action opens a page in this portal.</p></div><a href="${ctx}/manage">Back</a></div>
<section class="card">
    <c:choose>
        <c:when test="${empty actions}"><div class="empty"><p>No quick actions yet.</p></div></c:when>
        <c:otherwise>
            <div class="table-wrap">
                <table>
                    <thead><tr><th>Label</th><th>Destination</th><th>Visible</th><th></th></tr></thead>
                    <tbody>
                    <c:forEach var="action" items="${actions}">
                        <tr>
                            <td><c:out value="${action.label}"/></td>
                            <td><c:out value="${action.destination}"/></td>
                            <td><c:out value="${action.visible ? 'Yes' : 'No'}"/></td>
                            <td>
                                <a href="${ctx}/manage/actions?id=${action.id}">Edit</a>
                                <form method="post" action="${ctx}/manage/actions/delete" data-confirm="Remove this quick action?" style="display:inline;">
                                    <input type="hidden" name="csrfToken" value="${csrfToken}">
                                    <input type="hidden" name="id" value="${action.id}">
                                    <button class="text-button" type="submit">Delete</button>
                                </form>
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>
        </c:otherwise>
    </c:choose>
</section>
<section class="card" style="margin-top:16px; max-width:720px;">
    <h2 style="margin-bottom:12px;">${form.id > 0 ? 'Edit action' : 'New action'}</h2>
    <form method="post" action="${ctx}/manage/actions/save">
        <input type="hidden" name="csrfToken" value="${csrfToken}">
        <input type="hidden" name="id" value="${form.id}">
        <div class="form-grid">
            <div class="field"><label for="label">Label</label><input id="label" name="label" required maxlength="80" value="${fn:escapeXml(form.label)}"><span class="error"><c:out value="${errors.label}"/></span></div>
            <div class="field"><label for="iconName">Icon</label>
                <select id="iconName" name="iconName">
                    <c:forEach var="icon" items="${icons}"><option value="${icon}" ${form.iconName == icon ? 'selected' : ''}><c:out value="${icon}"/></option></c:forEach>
                </select><span class="error"><c:out value="${errors.iconName}"/></span>
            </div>
            <div class="field"><label for="destination">Destination</label><input id="destination" name="destination" required maxlength="160" value="${fn:escapeXml(form.destination)}"><span class="error"><c:out value="${errors.destination}"/></span></div>
            <div class="field"><label for="sortOrder">Sort order</label><input id="sortOrder" name="sortOrder" type="number" min="1" max="50" required value="${form.sortOrder}"><span class="error"><c:out value="${errors.sortOrder}"/></span></div>
        </div>
        <label><input type="checkbox" name="visible" ${form.visible ? 'checked' : ''}> Visible on the dashboard</label>
        <div style="margin-top:14px;"><button class="primary" type="submit">Save action</button></div>
    </form>
</section>
<%@ include file="fragments/layout-end.jsp" %>
