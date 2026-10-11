        </main>
        <footer class="footer">
            <span>&copy; <c:out value="${currentYear}"/> IRCTC. All rights reserved.</span>
            <nav>
                <a href="${pageContext.request.contextPath}/privacy">Privacy</a>
                <a href="${pageContext.request.contextPath}/terms">Terms</a>
                <a href="${pageContext.request.contextPath}/help">Help</a>
            </nav>
        </footer>
    </div>
</div>
<div class="toast-wrap" id="toasts">
    <c:if test="${not empty flashSuccess}"><div class="toast" role="status"><c:out value="${flashSuccess}"/></div></c:if>
    <c:if test="${not empty flashError}"><div class="toast error" role="status"><c:out value="${flashError}"/></div></c:if>
</div>
<dialog id="confirmDialog">
    <h2>Please confirm</h2>
    <p id="confirmText"></p>
    <form method="dialog" class="dialog-actions">
        <button class="secondary" value="cancel">Cancel</button>
        <button class="danger" id="confirmOk" value="ok">Confirm</button>
    </form>
</dialog>
<script src="${pageContext.request.contextPath}/assets/vendor/lucide.min.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/app.js"></script>
<c:if test="${pageScript == 'dashboard'}">
    <script src="${pageContext.request.contextPath}/assets/vendor/chart.umd.min.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/dashboard.js"></script>
</c:if>
</body>
</html>
