<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="robots" content="noindex">
    <meta name="csrf-token" content="${fn:escapeXml(csrfToken)}">
    <title><c:out value="${portalName}"/> · <c:out value="${pageTitle}"/></title>
    <link rel="icon" href="${ctx}/favicon.svg">
    <link rel="stylesheet" href="${ctx}/assets/css/app.css">
    <script>
        if (localStorage.getItem("sidebar") === "collapsed") {
            document.documentElement.classList.add("sidebar-collapsed");
        }
    </script>
</head>
<body data-context="${ctx}">
<div id="loadingBar" class="loading-bar"></div>
<div class="sidebar-backdrop"></div>
<div class="app-shell">
    <aside class="sidebar">
        <div class="brand">
            <img class="brand-logo" src="${ctx}/assets/images/brand-mark.svg" alt="">
            <div class="brand-copy">
                <strong>IRCTC</strong>
            </div>
            <button class="collapse-btn" id="collapseSidebar" type="button" aria-label="Collapse sidebar">
                <i data-lucide="panel-left"></i>
            </button>
        </div>
        <nav class="nav" aria-label="Primary">
            <a class="nav-link ${currentPath == '/dashboard' ? 'active' : ''}" href="${ctx}/dashboard"><i data-lucide="layout-dashboard"></i><span class="label">Dashboard</span></a>

            <div class="nav-group ${fn:startsWith(currentPath, '/bookings') ? 'open' : ''}">
                <div class="nav-row">
                    <a class="nav-link ${fn:startsWith(currentPath, '/bookings') ? 'active' : ''}" href="${ctx}/bookings"><i data-lucide="ticket"></i><span class="label">Bookings</span></a>
                    <button class="nav-expand" type="button" aria-label="Expand Bookings"><i data-lucide="chevron-down"></i></button>
                </div>
                <div class="submenu">
                    <a class="${currentPath == '/bookings' ? 'active' : ''}" href="${ctx}/bookings">All bookings</a>
                    <a class="${currentPath == '/bookings/new' ? 'active' : ''}" href="${ctx}/bookings/new">Record booking</a>
                </div>
            </div>

            <div class="nav-group ${fn:startsWith(currentPath, '/users') ? 'open' : ''}">
                <div class="nav-row">
                    <a class="nav-link ${fn:startsWith(currentPath, '/users') ? 'active' : ''}" href="${ctx}/users"><i data-lucide="users"></i><span class="label">Passengers</span></a>
                    <button class="nav-expand" type="button" aria-label="Expand Passengers"><i data-lucide="chevron-down"></i></button>
                </div>
                <div class="submenu">
                    <a href="${ctx}/users">All passengers</a>
                    <a href="${ctx}/users/new">Add passenger</a>
                </div>
            </div>

            <div class="nav-group ${fn:startsWith(currentPath, '/trains') ? 'open' : ''}">
                <div class="nav-row">
                    <a class="nav-link ${fn:startsWith(currentPath, '/trains') ? 'active' : ''}" href="${ctx}/trains"><i data-lucide="train-front"></i><span class="label">Train Management</span></a>
                    <button class="nav-expand" type="button" aria-label="Expand Train Management"><i data-lucide="chevron-down"></i></button>
                </div>
                <div class="submenu">
                    <a href="${ctx}/trains">All trains</a>
                    <a href="${ctx}/trains/new">Add train</a>
                    <a href="${ctx}/trains/schedule">Schedules</a>
                </div>
            </div>

            <a class="nav-link ${fn:startsWith(currentPath, '/reports') ? 'active' : ''}" href="${ctx}/reports"><i data-lucide="file-bar-chart"></i><span class="label">Reports</span></a>
            <div class="nav-group ${fn:startsWith(currentPath, '/employees') ? 'open' : ''}">
                <div class="nav-row">
                    <a class="nav-link ${fn:startsWith(currentPath, '/employees') ? 'active' : ''}" href="${ctx}/employees"><i data-lucide="id-card"></i><span class="label">Employee Management</span></a>
                    <button class="nav-expand" type="button" aria-label="Expand Employee Management"><i data-lucide="chevron-down"></i></button>
                </div>
                <div class="submenu">
                    <a href="${ctx}/employees">All employees</a>
                    <a href="${ctx}/employees/new">Add employee</a>
                </div>
            </div>
            <a class="nav-link ${currentPath == '/activities' ? 'active' : ''}" href="${ctx}/activities"><i data-lucide="scroll-text"></i><span class="label">System Logs</span></a>
            <a class="nav-link ${currentPath == '/services' ? 'active' : ''}" href="${ctx}/services"><i data-lucide="compass"></i><span class="label">Services</span></a>

            <div class="nav-group ${fn:startsWith(currentPath, '/manage') ? 'open' : ''}">
                <div class="nav-row">
                    <a class="nav-link ${fn:startsWith(currentPath, '/manage') ? 'active' : ''}" href="${ctx}/manage"><i data-lucide="panels-top-left"></i><span class="label">Website &amp; Dashboard</span></a>
                    <button class="nav-expand" type="button" aria-label="Expand Website and Dashboard Management"><i data-lucide="chevron-down"></i></button>
                </div>
                <div class="submenu">
                    <a class="${currentPath == '/manage/banner' ? 'active' : ''}" href="${ctx}/manage/banner">Dashboard content</a>
                    <a class="${currentPath == '/manage/statistics' ? 'active' : ''}" href="${ctx}/manage/statistics">Statistics</a>
                    <a class="${currentPath == '/manage/routes' ? 'active' : ''}" href="${ctx}/manage/routes">Routes</a>
                    <a class="${currentPath == '/manage/classes' ? 'active' : ''}" href="${ctx}/manage/classes">Booking classes</a>
                    <a class="${currentPath == '/manage/actions' ? 'active' : ''}" href="${ctx}/manage/actions">Quick actions</a>
                    <a class="${currentPath == '/manage/announcements' ? 'active' : ''}" href="${ctx}/manage/announcements">Announcements</a>
                </div>
            </div>
        </nav>
        <div class="sidebar-extra">
            <div class="status-card">
                <span class="status-dot ${dbHealthy ? '' : 'down'}"></span>
                <div>
                    <strong><c:out value="${dbHealthy ? 'All systems operational' : 'Database unavailable'}"/></strong>
                    <small>Development portal</small>
                </div>
            </div>
            <div class="promo-card">
                <img src="${ctx}/assets/images/train-banner.jpg" alt="">
                <div>
                    <strong>IRCTC</strong>
                    <span>Connecting India</span><br>
                    <small>One journey at a time</small>
                </div>
            </div>
        </div>
        <div class="sidebar-foot">
            <div class="dropdown">
                <button class="side-profile" id="sideProfileButton" type="button" aria-expanded="false" aria-haspopup="true">
                    <span class="avatar"><i data-lucide="user"></i></span>
                    <span class="side-profile-copy"><strong><c:out value="${adminName}"/></strong><small>Administrator</small></span>
                </button>
                <div id="sideProfilePanel" class="dropdown-panel" hidden>
                    <a class="menu-link" href="${ctx}/profile">My Profile</a>
                    <a class="menu-link" href="${ctx}/settings">Settings</a>
                    <form method="post" action="${ctx}/logout">
                        <input type="hidden" name="csrfToken" value="${fn:escapeXml(csrfToken)}">
                        <button class="menu-link" type="submit">Logout</button>
                    </form>
                </div>
            </div>
        </div>
    </aside>
    <div class="main">
        <header class="topbar">
            <button class="icon-button mobile-only" id="openSidebar" type="button" aria-label="Open menu"><i data-lucide="menu"></i></button>
            <div class="search-wrap">
                <form action="${ctx}/search" method="get" role="search">
                    <i data-lucide="search"></i>
                    <input id="globalSearch" name="q" value="${fn:escapeXml(param.q)}" placeholder="Search employees, bookings, trains..." autocomplete="off" aria-label="Search records">
                    <kbd>Ctrl + K</kbd>
                </form>
                <div id="searchPanel" class="search-panel" hidden></div>
            </div>
            <div class="top-actions">
                <div class="dropdown">
                    <button class="bell-btn" id="notifButton" type="button" aria-expanded="false" aria-label="Notifications">
                        <i data-lucide="bell"></i>
                        <c:if test="${unreadCount > 0}"><span class="badge-count"><c:out value="${unreadCount}"/></span></c:if>
                    </button>
                    <div id="notifPanel" class="dropdown-panel" hidden>
                        <c:forEach var="item" items="${headerNotifications}">
                            <a class="notif-item" href="${ctx}/activities">
                                <span class="activity-icon"><i data-lucide="${item.icon}"></i></span>
                                <span><strong><c:out value="${item.message}"/></strong><br><small><c:out value="${item.timeLabel}"/></small></span>
                            </a>
                        </c:forEach>
                        <c:if test="${empty headerNotifications}"><p class="muted" style="padding:10px;">No activity yet.</p></c:if>
                        <form method="post" action="${ctx}/notifications/read">
                            <input type="hidden" name="csrfToken" value="${fn:escapeXml(csrfToken)}">
                            <input type="hidden" name="returnTo" value="${fn:escapeXml(currentPath)}">
                            <button class="text-button" id="markRead" type="submit">Mark all read</button>
                            <a class="text-button" href="${ctx}/activities">View all</a>
                        </form>
                    </div>
                </div>
                <div class="dropdown">
                    <button class="profile-btn" id="profileButton" type="button" aria-expanded="false">
                        <span class="avatar"><i data-lucide="user"></i></span>
                        <span class="profile-copy"><strong><c:out value="${adminName}"/></strong><small>Administrator</small></span>
                    </button>
                    <div id="profilePanel" class="dropdown-panel" hidden>
                        <p class="muted" style="padding:8px 10px;"><c:out value="${adminEmail}"/></p>
                        <a class="menu-link" href="${ctx}/profile">My Profile</a>
                        <a class="menu-link" href="${ctx}/settings">Settings</a>
                        <form method="post" action="${ctx}/logout">
                            <input type="hidden" name="csrfToken" value="${fn:escapeXml(csrfToken)}">
                            <button class="menu-link" type="submit">Log out</button>
                        </form>
                    </div>
                </div>
            </div>
        </header>
        <main class="content">
            <c:if test="${sampleData and currentPath != '/dashboard'}">
                <div class="sample-note">Development Sample Data. These records were loaded by the development seed script and are not live IRCTC information.</div>
            </c:if>
