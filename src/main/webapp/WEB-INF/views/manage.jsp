<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ include file="fragments/layout-start.jsp" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<div class="page-head"><div><h1>Website &amp; Dashboard Management</h1><p class="muted">Edit what the dashboard shows. Booking totals stay calculated from stored records.</p></div></div>
<div class="manage-grid">
    <a class="manage-card" href="${ctx}/manage/banner"><strong>Dashboard content</strong><p>Welcome title, description, badge, and banner image.</p></a>
    <a class="manage-card" href="${ctx}/manage/statistics"><strong>Bookings &amp; statistics</strong><p>Live totals for bookings, passengers, trains, and revenue.</p></a>
    <a class="manage-card" href="${ctx}/trains"><strong>Train management</strong><p>Train number, name, route, schedule, and status.</p></a>
    <a class="manage-card" href="${ctx}/manage/routes"><strong>Routes</strong><p>Route totals from bookings, and station names used on the dashboard.</p></a>
    <a class="manage-card" href="${ctx}/manage/classes"><strong>Booking classes</strong><p>Names and colors for the class chart. Counts come from bookings.</p></a>
    <a class="manage-card" href="${ctx}/manage/actions"><strong>Quick actions</strong><p>Labels, icons, destinations, and visibility.</p></a>
    <a class="manage-card" href="${ctx}/manage/announcements"><strong>Announcements</strong><p>Notices shown on the dashboard, and how many activities appear.</p></a>
</div>
<%@ include file="fragments/layout-end.jsp" %>
