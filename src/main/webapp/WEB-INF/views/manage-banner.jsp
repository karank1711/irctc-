<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ include file="fragments/layout-start.jsp" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<div class="page-head"><div><h1>Dashboard content</h1><p class="muted">The signed-in name is added after the title.</p></div><a href="${ctx}/manage">Back</a></div>
<section class="card" style="max-width:760px;">
    <form method="post" action="${ctx}/manage/banner" enctype="multipart/form-data">
        <input type="hidden" name="csrfToken" value="${csrfToken}">
        <div class="field"><label for="bannerTitle">Welcome title</label><input id="bannerTitle" name="bannerTitle" required maxlength="80" value="${fn:escapeXml(banner.title)}"><span class="error"><c:out value="${errors.bannerTitle}"/></span></div>
        <div class="field"><label for="bannerDescription">Description</label><textarea class="textarea" id="bannerDescription" name="bannerDescription" required maxlength="240" rows="3">${fn:escapeXml(banner.description)}</textarea><span class="error"><c:out value="${errors.bannerDescription}"/></span></div>
        <div class="field"><label for="bannerBadge">Badge</label><input id="bannerBadge" name="bannerBadge" required maxlength="60" value="${fn:escapeXml(banner.badge)}"><span class="error"><c:out value="${errors.bannerBadge}"/></span></div>
        <div class="field"><label for="bannerImage">Image address</label><input id="bannerImage" name="bannerImage" required maxlength="240" value="${fn:escapeXml(banner.image)}"><small class="muted">A project path such as /assets/images/train-banner.jpg, an uploaded file, or an https address.</small><span class="error"><c:out value="${errors.bannerImage}"/></span></div>
        <div class="field"><label for="bannerFile">Upload a new banner image</label><input id="bannerFile" name="bannerFile" type="file" accept="image/jpeg,image/png,image/webp"><span class="error"><c:out value="${errors.bannerFile}"/></span></div>
        <div class="field"><label for="activityLimit">Activities on the dashboard</label>
            <select id="activityLimit" name="activityLimit">
                <option value="3" ${banner.activityLimit == 3 ? 'selected' : ''}>3</option>
                <option value="5" ${banner.activityLimit == 5 ? 'selected' : ''}>5</option>
                <option value="8" ${banner.activityLimit == 8 ? 'selected' : ''}>8</option>
            </select>
            <span class="error"><c:out value="${errors.activityLimit}"/></span>
        </div>
        <button class="primary" type="submit">Save banner</button>
    </form>
</section>
<%@ include file="fragments/layout-end.jsp" %>
