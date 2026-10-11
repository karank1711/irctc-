<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ include file="fragments/layout-start.jsp" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<div class="page-head"><div><h1>Announcements</h1><p class="muted">Active notices appear above the welcome banner. Activity history itself is recorded from real actions.</p></div><a href="${ctx}/manage">Back</a></div>
<section class="card">
    <c:choose>
        <c:when test="${empty announcements}"><div class="empty"><p>No announcements yet.</p></div></c:when>
        <c:otherwise>
            <div class="table-wrap">
                <table>
                    <thead><tr><th>Title</th><th>Status</th><th>Created</th><th></th></tr></thead>
                    <tbody>
                    <c:forEach var="item" items="${announcements}">
                        <tr>
                            <td><c:out value="${item.title}"/></td>
                            <td><c:out value="${item.active ? 'Active' : 'Hidden'}"/></td>
                            <td><c:out value="${item.createdLabel}"/></td>
                            <td>
                                <a href="${ctx}/manage/announcements?id=${item.id}">Edit</a>
                                <form method="post" action="${ctx}/manage/announcements/delete" data-confirm="Delete this announcement?" style="display:inline;">
                                    <input type="hidden" name="csrfToken" value="${csrfToken}">
                                    <input type="hidden" name="id" value="${item.id}">
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
    <h2 style="margin-bottom:12px;">${form.id > 0 ? 'Edit announcement' : 'New announcement'}</h2>
    <form method="post" action="${ctx}/manage/announcements/save">
        <input type="hidden" name="csrfToken" value="${csrfToken}">
        <input type="hidden" name="id" value="${form.id}">
        <div class="field"><label for="title">Title</label><input id="title" name="title" required maxlength="120" value="${fn:escapeXml(form.title)}"><span class="error"><c:out value="${errors.title}"/></span></div>
        <div class="field"><label for="message">Message</label><textarea class="textarea" id="message" name="message" required maxlength="500" rows="3">${fn:escapeXml(form.message)}</textarea><span class="error"><c:out value="${errors.message}"/></span></div>
        <label><input type="checkbox" name="active" ${form.active ? 'checked' : ''}> Show on the dashboard</label>
        <div style="margin-top:14px;"><button class="primary" type="submit">Save announcement</button></div>
    </form>
</section>
<%@ include file="fragments/layout-end.jsp" %>
