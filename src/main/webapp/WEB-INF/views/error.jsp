<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Something went wrong</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/app.css">
</head>
<body class="login-body">
<main class="login-card">
    <h1><c:out value="${empty errorTitle ? 'Something went wrong' : errorTitle}"/></h1>
    <p><c:out value="${empty errorMessage ? 'That page is not available.' : errorMessage}"/></p>
    <p><a class="primary" href="${pageContext.request.contextPath}/dashboard">Back to dashboard</a></p>
</main>
</body>
</html>
