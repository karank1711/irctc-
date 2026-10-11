<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ include file="fragments/layout-start.jsp" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<div class="page-head"><div><h1>Users</h1><p class="muted">Passenger accounts managed by this portal.</p></div><a class="primary" href="${ctx}/users/new">Add user</a></div>
<form class="filters" method="get" action="${ctx}/users">
    <label class="filter-search">
        <i data-lucide="search"></i>
        <input name="q" value="${fn:escapeXml(query)}" placeholder="Search passengers by name, email, or phone" aria-label="Search passengers">
    </label>
    <select name="status" aria-label="Passenger status"><option value="">All statuses</option><option value="ACTIVE" ${status == 'ACTIVE' ? 'selected' : ''}>Active</option><option value="INACTIVE" ${status == 'INACTIVE' ? 'selected' : ''}>Inactive</option></select>
    <button class="primary icon-filter" type="submit" title="Filter" aria-label="Filter"><i data-lucide="list-filter"></i></button>
    <c:if test="${not empty query or not empty status}"><a class="clear-filter" href="${ctx}/users">Clear</a></c:if>
</form>
<section class="card">
    <c:choose>
        <c:when test="${result.withoutRows}"><div class="empty"><h3>No users found</h3><p>Add a user or change the filter.</p></div></c:when>
        <c:otherwise>
            <div class="table-wrap"><table>
                <thead><tr><th>Name</th><th>Email</th><th>Phone</th><th>Status</th><th></th></tr></thead>
                <tbody>
                <c:forEach var="user" items="${result.items}">
                    <tr>
                        <td><a href="${ctx}/users/view?id=${user.id}"><c:out value="${user.fullName}"/></a></td>
                        <td><c:out value="${user.email}"/></td>
                        <td><c:out value="${user.phone}"/></td>
                        <td><span class="badge badge-${user.status == 'ACTIVE' ? 'active' : 'inactive'}"><c:out value="${user.statusLabel}"/></span></td>
                        <td><a href="${ctx}/users/edit?id=${user.id}">Edit</a></td>
                    </tr>
                </c:forEach>
                </tbody>
            </table></div>
            <%@ include file="fragments/pagination.jsp" %>
        </c:otherwise>
    </c:choose>
</section>
<%@ include file="fragments/layout-end.jsp" %>
