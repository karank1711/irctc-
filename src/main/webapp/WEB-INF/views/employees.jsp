<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ include file="fragments/layout-start.jsp" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<div class="page-head"><div><h1>Employees</h1><p class="muted">Staff records for this administration portal.</p></div><a class="primary" href="${ctx}/employees/new">Add employee</a></div>
<form class="filters" method="get" action="${ctx}/employees">
    <label class="filter-search">
        <i data-lucide="search"></i>
        <input name="q" value="${fn:escapeXml(query)}" placeholder="Search employees, bookings, trains..." aria-label="Search employees">
    </label>
    <select name="status" aria-label="Employee status"><option value="">All statuses</option><option value="ACTIVE" ${status == 'ACTIVE' ? 'selected' : ''}>Active</option><option value="INACTIVE" ${status == 'INACTIVE' ? 'selected' : ''}>Inactive</option></select>
    <button class="primary icon-filter" type="submit" title="Filter" aria-label="Filter"><i data-lucide="list-filter"></i></button>
    <c:if test="${not empty query or not empty status}"><a class="clear-filter" href="${ctx}/employees">Clear</a></c:if>
</form>
<section class="card">
    <c:choose>
        <c:when test="${result.withoutRows}"><div class="empty"><h3>No employees found</h3><p>Add an employee or change the filter.</p></div></c:when>
        <c:otherwise>
            <div class="table-wrap"><table>
                <thead><tr><th>Code</th><th>Name</th><th>Department</th><th>Designation</th><th>Status</th><th></th></tr></thead>
                <tbody>
                <c:forEach var="employee" items="${result.items}">
                    <tr>
                        <td><c:out value="${employee.employeeCode}"/></td>
                        <td><a href="${ctx}/employees/view?id=${employee.id}"><c:out value="${employee.fullName}"/></a></td>
                        <td><c:out value="${employee.department}"/></td>
                        <td><c:out value="${employee.designation}"/></td>
                        <td><span class="badge badge-${employee.status == 'ACTIVE' ? 'active' : 'inactive'}"><c:out value="${employee.statusLabel}"/></span></td>
                        <td><a href="${ctx}/employees/edit?id=${employee.id}">Edit</a></td>
                    </tr>
                </c:forEach>
                </tbody>
            </table></div>
            <%@ include file="fragments/pagination.jsp" %>
        </c:otherwise>
    </c:choose>
</section>
<%@ include file="fragments/layout-end.jsp" %>
