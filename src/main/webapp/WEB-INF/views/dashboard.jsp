<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Dashboard" scope="request"/>
<c:set var="pageScript" value="dashboard" scope="request"/>
<%@ include file="fragments/layout-start.jsp" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:if test="${not empty dashboardError}"><div class="card"><p><c:out value="${dashboardError}"/></p></div></c:if>
<c:if test="${empty dashboardError}">
<c:if test="${not empty announcements}">
    <c:forEach var="note" items="${announcements}">
        <div class="announcement"><strong><c:out value="${note.title}"/></strong> <c:out value="${note.message}"/></div>
    </c:forEach>
</c:if>
<section class="hero">
    <img class="hero-photo" src="${ctx}/assets/images/train-banner.jpg" alt="">
    <div class="hero-copy">
        <span class="hero-accent" aria-hidden="true"></span>
        <div>
            <p class="hero-kicker">Welcome Back,</p>
            <h1>IRCTC Employee Portal</h1>
            <p class="hero-lead">Manage operations, monitor bookings and ensure smooth travel experience for millions.</p>
        </div>
    </div>
    <aside class="hero-status">
        <span class="hero-status-dot ${dbHealthy ? '' : 'down'}"></span>
        <div>
            <strong><c:out value="${dbHealthy ? 'All Systems Operational' : 'Database unavailable'}"/></strong>
            <small>Last updated: <c:out value="${dashboard.generatedLabel}"/></small>
        </div>
    </aside>
</section>
<section class="dashboard-grid">
    <div class="dashboard-primary">
        <section class="kpi-grid">
            <c:forEach var="metric" items="${dashboard.metrics}">
                <c:choose>
                    <c:when test="${metric.label == 'Total Bookings'}"><c:set var="metricHref" value="/bookings"/></c:when>
                    <c:when test="${metric.label == 'Active Passengers'}"><c:set var="metricHref" value="/users?status=ACTIVE"/></c:when>
                    <c:when test="${metric.label == 'Total Trains'}"><c:set var="metricHref" value="/trains"/></c:when>
                    <c:otherwise><c:set var="metricHref" value="/reports"/></c:otherwise>
                </c:choose>
                <a class="card metric tone-${metric.tone}" href="${ctx}${metricHref}">
                    <div class="metric-top">
                        <span class="metric-icon ${metric.tone}"><i data-lucide="${metric.icon}"></i></span>
                        <c:out value="${metric.sparklineSvg}" escapeXml="false"/>
                    </div>
                    <p class="metric-label"><c:out value="${metric.label}"/></p>
                    <p class="metric-value" data-value="${metric.rawValue}" data-final="${fn:escapeXml(metric.displayValue)}"><c:out value="${metric.displayValue}"/></p>
                    <c:choose>
                        <c:when test="${metric.comparisonAvailable}">
                            <p class="metric-change ${metric.increase ? 'up' : 'down'}"><c:out value="${metric.changeLabel}"/> <span class="muted">vs. last week</span></p>
                        </c:when>
                        <c:otherwise><p class="metric-change muted">Comparison unavailable</p></c:otherwise>
                    </c:choose>
                </a>
            </c:forEach>
        </section>
        <section class="split">
            <article class="card">
                <div class="card-head">
                    <h2><i data-lucide="chart-line"></i> Booking Trends</h2>
                    <select id="periodSelect" class="period-select" aria-label="Booking trend period">
                        <option value="7">Last 7 Days</option>
                        <option value="30">Last 30 Days</option>
                        <option value="90">Last 90 Days</option>
                    </select>
                </div>
                <div class="chart-box"><canvas id="bookingChart"></canvas></div>
                <p id="chartEmpty" class="muted" ${dashboard.bookingsChart.withoutRows ? '' : 'hidden'}>No bookings in this period.</p>
                <script id="chart-data" type="application/json"><c:out value="${chartJson}" escapeXml="false"/></script>
            </article>
            <article class="card">
                <div class="card-head"><h2><i data-lucide="pie-chart"></i> Bookings by Class</h2></div>
                <c:set var="offeredCount" value="0"/>
                <c:forEach var="service" items="${dashboard.services}"><c:if test="${service.offered && service.bookingCount > 0}"><c:set var="offeredCount" value="${offeredCount + 1}"/></c:if></c:forEach>
                <c:choose>
                    <c:when test="${offeredCount == 0}"><div class="empty"><p>No class bookings yet.</p></div></c:when>
                    <c:otherwise>
                        <div class="doughnut-wrap">
                            <div class="doughnut">
                                <canvas id="serviceChart"></canvas>
                                <div class="doughnut-center"><strong><c:out value="${dashboard.offeredBookingTotalLabel}"/></strong><span>Total Bookings</span></div>
                            </div>
                            <ul class="legend">
                                <c:forEach var="service" items="${dashboard.services}">
                                    <c:if test="${service.offered}">
                                        <li><span><i class="swatch" style="background:${service.color}"></i><c:out value="${service.name}"/></span><strong><c:out value="${service.percentLabel}"/></strong></li>
                                    </c:if>
                                </c:forEach>
                            </ul>
                        </div>
                    </c:otherwise>
                </c:choose>
                <script id="service-data" type="application/json"><c:out value="${serviceJson}" escapeXml="false"/></script>
            </article>
        </section>
        <section class="split">
            <article class="card">
                <div class="card-head"><h2><i data-lucide="ticket"></i> Recent Bookings</h2><a href="${ctx}/bookings">View all</a></div>
                <c:choose>
                    <c:when test="${empty dashboard.recentBookings}"><div class="empty"><p>No bookings yet.</p></div></c:when>
                    <c:otherwise>
                        <div class="table-wrap">
                            <table class="fit-table">
                                <thead><tr><th>PNR No.</th><th>Train No.</th><th>From - To</th><th>Class</th><th>Booking Date</th><th>Status</th></tr></thead>
                                <tbody>
                                <c:forEach var="booking" items="${dashboard.recentBookings}">
                                    <tr>
                                        <td><a href="${ctx}/bookings/view?id=${booking.id}"><c:out value="${booking.pnr}"/></a></td>
                                        <td><c:out value="${booking.trainNumber}"/></td>
                                        <td class="route-cell" title="${fn:escapeXml(booking.routeLabel)}"><c:out value="${booking.routeLabel}"/></td>
                                        <td><c:out value="${booking.classLabel}"/></td>
                                        <td><c:out value="${booking.bookingDateLabel}"/></td>
                                        <td class="status-cell"><span class="badge badge-${booking.statusTone}"><c:out value="${booking.statusLabel}"/></span></td>
                                    </tr>
                                </c:forEach>
                                </tbody>
                            </table>
                        </div>
                    </c:otherwise>
                </c:choose>
            </article>
            <article class="card">
                <div class="card-head"><h2><i data-lucide="map-pin"></i> Top Routes</h2><a href="${ctx}/reports">View all</a></div>
                <c:choose>
                    <c:when test="${empty dashboard.topRoutes}"><div class="empty"><p>No routes yet.</p></div></c:when>
                    <c:otherwise>
                        <c:forEach var="route" items="${dashboard.topRoutes}">
                            <div class="route-row"><span class="route-icon"><i data-lucide="train-front"></i></span><span><c:out value="${route.routeLabel}"/></span><strong><c:out value="${route.bookingsLabel}"/></strong></div>
                        </c:forEach>
                    </c:otherwise>
                </c:choose>
            </article>
        </section>
    </div>
    <aside class="dashboard-side">
        <section class="card">
            <div class="card-head"><h2>Today's Activity</h2><span class="icon-ghost"><i data-lucide="calendar"></i></span></div>
            <div class="duty"><span class="duty-dot"></span><div><strong>On Duty</strong><p class="muted">Signed in <c:out value="${loginLabel}"/></p></div></div>
        </section>
        <section class="card">
            <div class="card-head"><h2>Quick Actions</h2></div>
            <c:choose>
                <c:when test="${empty quickActions}"><div class="empty"><p>No quick actions are visible. Add one under Website &amp; Dashboard.</p></div></c:when>
                <c:otherwise>
                    <c:forEach var="action" items="${quickActions}">
                        <a class="quick-link" href="${ctx}${fn:escapeXml(action.destination)}"><i data-lucide="${fn:escapeXml(action.iconName)}"></i><span><c:out value="${action.label}"/></span><i data-lucide="chevron-right"></i></a>
                    </c:forEach>
                </c:otherwise>
            </c:choose>
        </section>
        <section class="card">
            <div class="card-head"><h2>Recent Activities</h2><a href="${ctx}/activities">View all</a></div>
            <c:choose>
                <c:when test="${empty dashboard.activities}"><div class="empty"><p>No activity recorded.</p></div></c:when>
                <c:otherwise>
                    <c:forEach var="item" items="${dashboard.activities}">
                        <div class="activity-item">
                            <span class="activity-icon"><i data-lucide="${item.icon}"></i></span>
                            <span class="activity-copy"><p><c:out value="${item.message}"/></p><small class="muted"><c:out value="${item.timeLabel}"/></small></span>
                        </div>
                    </c:forEach>
                </c:otherwise>
            </c:choose>
        </section>
    </aside>
</section>
</c:if>
<%@ include file="fragments/layout-end.jsp" %>
