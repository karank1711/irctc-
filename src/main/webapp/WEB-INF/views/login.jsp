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
    <title>Sign in · IRCTC Admin</title>
    <link rel="icon" href="${ctx}/favicon.svg">
    <link rel="stylesheet" href="${ctx}/assets/css/app.css">
</head>
<body class="login-body">
<main class="login-card">
    <div class="login-brand"><img src="${ctx}/assets/images/brand-mark.svg" alt=""><span>IRCTC</span></div>
    <h1>IRCTC Administration</h1>
    <p class="muted">Sign in with the administrator account configured on this server.</p>
    <c:if test="${not empty startupError}"><p class="form-error"><c:out value="${startupError}"/></p></c:if>
    <c:if test="${not empty loginError}"><p class="form-error"><c:out value="${loginError}"/></p></c:if>
    <c:if test="${not empty loginMessage}"><p class="muted"><c:out value="${loginMessage}"/></p></c:if>
    <form id="loginForm" method="post" action="${ctx}/login">
        <input type="hidden" name="csrfToken" value="${fn:escapeXml(csrfToken)}">
        <div class="field">
            <label for="email">Email</label>
            <div class="login-input">
                <i data-lucide="mail"></i>
                <input id="email" name="email" type="email" autocomplete="username" required maxlength="150" placeholder="Enter your email address" value="${fn:escapeXml(email)}">
            </div>
        </div>
        <div class="field">
            <label for="password">Password</label>
            <div class="login-input">
                <i data-lucide="lock"></i>
                <input id="password" name="password" type="password" autocomplete="current-password" required maxlength="72" placeholder="Enter your password">
                <button class="ghost-icon" id="togglePassword" type="button" aria-label="Show password" aria-pressed="false"><i data-lucide="eye"></i></button>
            </div>
        </div>
        <button class="sign-in" id="signInButton" type="submit">Sign in <i data-lucide="arrow-right"></i></button>
    </form>
</main>
<script src="${ctx}/assets/vendor/lucide.min.js"></script>
<script>
    if (window.lucide) lucide.createIcons({ icons: lucide.icons });
    const password = document.getElementById("password");
    const toggle = document.getElementById("togglePassword");
    toggle?.addEventListener("click", function () {
        const visible = password.type === "text";
        password.type = visible ? "password" : "text";
        toggle.setAttribute("aria-label", visible ? "Show password" : "Hide password");
        toggle.setAttribute("aria-pressed", String(!visible));
        toggle.innerHTML = visible ? '<i data-lucide="eye"></i>' : '<i data-lucide="eye-off"></i>';
        if (window.lucide) lucide.createIcons({ icons: lucide.icons });
    });
    document.getElementById("loginForm")?.addEventListener("submit", function () {
        const button = document.getElementById("signInButton");
        if (!button) return;
        button.disabled = true;
        button.textContent = "Signing in";
    });
</script>
</body>
</html>
