(function () {
    const root = document.documentElement;
    const sidebar = document.querySelector(".sidebar");
    const backdrop = document.querySelector(".sidebar-backdrop");
    const csrf = document.querySelector('meta[name="csrf-token"]')?.content || "";

    document.getElementById("collapseSidebar")?.addEventListener("click", function () {
        const collapsed = root.classList.toggle("sidebar-collapsed");
        localStorage.setItem("sidebar", collapsed ? "collapsed" : "expanded");
        if (collapsed) {
            document.querySelectorAll(".nav-group.open").forEach(function (group) { group.classList.remove("open"); });
        }
        hideNavTip();
        syncProfileLabel();
    });
    document.getElementById("openSidebar")?.addEventListener("click", function () {
        sidebar?.classList.add("open");
        backdrop?.classList.add("show");
        document.body.classList.add("drawer-open");
    });
    backdrop?.addEventListener("click", closeSidebar);
    function closeSidebar() {
        sidebar?.classList.remove("open");
        backdrop?.classList.remove("show");
        document.body.classList.remove("drawer-open");
    }

    document.querySelectorAll(".nav-expand").forEach(function (button) {
        button.addEventListener("click", function () {
            if (root.classList.contains("sidebar-collapsed") && window.innerWidth > 860) {
                root.classList.remove("sidebar-collapsed");
                localStorage.setItem("sidebar", "expanded");
            }
            button.closest(".nav-group")?.classList.toggle("open");
            hideNavTip();
        });
    });

    const navTip = document.createElement("div");
    navTip.id = "navTip";
    navTip.className = "nav-tip";
    navTip.setAttribute("role", "tooltip");
    document.body.appendChild(navTip);
    let navTipTarget = null;

    function collapsedIcons() {
        return root.classList.contains("sidebar-collapsed") && window.innerWidth > 860;
    }

    function navLabel(item) {
        const label = item.querySelector(":scope > .label, :scope .label");
        if (label) return label.textContent.trim();
        const name = item.querySelector(".side-profile-copy strong");
        return name ? name.textContent.trim() : "";
    }

    function hideNavTip() {
        navTip.classList.remove("is-on");
        navTip.textContent = "";
        navTip.style.left = "-9999px";
        if (navTipTarget) navTipTarget.removeAttribute("aria-describedby");
        navTipTarget = null;
    }

    function showNavTip(item) {
        if (!collapsedIcons()) {
            hideNavTip();
            return;
        }
        const text = navLabel(item);
        if (!text || !sidebar) return;
        if (navTipTarget && navTipTarget !== item) navTipTarget.removeAttribute("aria-describedby");
        navTipTarget = item;
        navTip.textContent = text;
        item.setAttribute("aria-describedby", "navTip");
        const icon = item.getBoundingClientRect();
        const bar = sidebar.getBoundingClientRect();
        navTip.style.top = (icon.top + icon.height / 2) + "px";
        navTip.style.left = (bar.right + 10) + "px";
        navTip.style.transform = "translateY(-50%)";
        navTip.classList.add("is-on");
    }

    function syncProfileLabel() {
        const profile = document.getElementById("sideProfileButton");
        if (!profile) return;
        if (collapsedIcons()) {
            const text = navLabel(profile);
            if (text) profile.setAttribute("aria-label", text);
        } else {
            profile.removeAttribute("aria-label");
        }
    }

    document.querySelectorAll(".nav-link").forEach(function (item) {
        const text = navLabel(item);
        if (text) item.setAttribute("aria-label", text);
    });
    syncProfileLabel();
    document.querySelectorAll(".nav-link, #sideProfileButton").forEach(function (item) {
        item.addEventListener("mouseenter", function () { showNavTip(item); });
        item.addEventListener("mouseleave", function () {
            if (!item.matches(":focus-visible")) hideNavTip();
        });
        item.addEventListener("focus", function () {
            if (item.matches(":focus-visible")) showNavTip(item);
        });
        item.addEventListener("blur", hideNavTip);
    });
    document.querySelector(".nav")?.addEventListener("scroll", hideNavTip);
    window.addEventListener("resize", function () {
        hideNavTip();
        syncProfileLabel();
    });
    sidebar?.querySelectorAll("a").forEach(function (link) {
        link.addEventListener("click", function () {
            if (window.innerWidth <= 860) closeSidebar();
        });
    });

    document.addEventListener("keydown", function (event) {
        if ((event.ctrlKey || event.metaKey) && event.key.toLowerCase() === "k") {
            event.preventDefault();
            document.getElementById("globalSearch")?.focus();
        }
        if (event.key === "Escape") {
            closePanels();
            closeSidebar();
        }
    });

    const searchInput = document.getElementById("globalSearch");
    const searchPanel = document.getElementById("searchPanel");
    let searchTimer;
    searchInput?.addEventListener("input", function () {
        window.clearTimeout(searchTimer);
        const query = searchInput.value.trim();
        if (query.length < 2) {
            hide(searchPanel);
            return;
        }
        searchTimer = window.setTimeout(function () {
            showLoading(true);
            fetch(contextPath() + "/api/search?q=" + encodeURIComponent(query), { headers: { "Accept": "application/json" } })
                .then(function (response) { return response.json(); })
                .then(function (body) { renderSearch(body.results || []); })
                .catch(function () { renderSearch([]); })
                .finally(function () { showLoading(false); });
        }, 250);
    });

    function renderSearch(results) {
        if (!searchPanel) return;
        searchPanel.replaceChildren();
        if (!results.length) {
            const empty = document.createElement("p");
            empty.className = "muted";
            empty.style.padding = "10px";
            empty.textContent = "No matching records.";
            searchPanel.appendChild(empty);
        } else {
            results.forEach(function (hit) {
                const link = document.createElement("a");
                link.className = "search-hit";
                link.href = contextPath() + hit.url;
                const text = document.createElement("span");
                const title = document.createElement("strong");
                title.textContent = hit.title;
                const sub = document.createElement("small");
                sub.textContent = hit.type + " · " + hit.subtitle;
                text.append(title, document.createElement("br"), sub);
                link.appendChild(text);
                searchPanel.appendChild(link);
            });
        }
        searchPanel.hidden = false;
    }

    document.getElementById("notifButton")?.addEventListener("click", function () {
        togglePanel("notifPanel", this);
    });
    document.getElementById("profileButton")?.addEventListener("click", function () {
        togglePanel("profilePanel", this);
    });
    document.getElementById("sideProfileButton")?.addEventListener("click", function () {
        togglePanel("sideProfilePanel", this);
    });
    document.addEventListener("click", function (event) {
        if (!event.target.closest(".dropdown") && !event.target.closest(".search-wrap")) {
            closePanels();
        }
    });

    function togglePanel(id, button) {
        const panel = document.getElementById(id);
        if (!panel) return;
        const open = panel.hidden;
        closePanels();
        panel.hidden = !open;
        button.setAttribute("aria-expanded", String(open));
    }
    function closePanels() {
        document.querySelectorAll(".dropdown-panel, #searchPanel").forEach(function (panel) { panel.hidden = true; });
    }
    function hide(element) { if (element) element.hidden = true; }

    document.getElementById("markRead")?.addEventListener("click", function (event) {
        event.preventDefault();
        showLoading(true);
        fetch(contextPath() + "/api/notifications/read", {
            method: "POST",
            headers: { "X-CSRF-Token": csrf, "Accept": "application/json" }
        }).then(function (response) {
            if (!response.ok) throw new Error("read failed");
            document.querySelectorAll(".badge-count").forEach(function (badge) { badge.remove(); });
            toast("Notifications marked as read.");
        }).catch(function () {
            toast("Notifications could not be updated.", true);
        }).finally(function () { showLoading(false); });
    });

    const dialog = document.getElementById("confirmDialog");
    document.addEventListener("submit", function (event) {
        const form = event.target;
        if (!(form instanceof HTMLFormElement) || !form.dataset.confirm || form.dataset.confirmed === "true") {
            return;
        }
        event.preventDefault();
        const text = document.getElementById("confirmText");
        if (text) text.textContent = form.dataset.confirm;
        if (typeof dialog.showModal === "function") {
            dialog.showModal();
            dialog.addEventListener("close", function handle() {
                dialog.removeEventListener("close", handle);
                if (dialog.returnValue === "ok") {
                    form.dataset.confirmed = "true";
                    form.requestSubmit();
                }
            }, { once: true });
        } else if (window.confirm(form.dataset.confirm)) {
            form.dataset.confirmed = "true";
            form.requestSubmit();
        }
    });

    document.querySelectorAll(".toast").forEach(function (item) {
        window.setTimeout(function () { item.remove(); }, 4200);
    });

    document.querySelectorAll("form.filters select").forEach(function (select) {
        select.addEventListener("change", function () {
            const page = select.form.querySelector("input[name='page']");
            if (page) page.value = "1";
            if (typeof select.form.requestSubmit === "function") {
                select.form.requestSubmit();
            } else {
                select.form.submit();
            }
        });
    });

    document.querySelectorAll("[data-train-select]").forEach(function (select) {
        select.addEventListener("change", function () {
            const option = select.selectedOptions[0];
            const source = document.querySelector("[data-source-select]");
            const destination = document.querySelector("[data-destination-select]");
            if (source && option?.dataset.source) source.value = option.dataset.source;
            if (destination && option?.dataset.destination) destination.value = option.dataset.destination;
        });
    });

    function contextPath() {
        return document.body.dataset.context || "";
    }
    function showLoading(active) {
        document.getElementById("loadingBar")?.classList.toggle("show", active);
    }
    function toast(message, isError) {
        const wrap = document.getElementById("toasts");
        if (!wrap) return;
        const item = document.createElement("div");
        item.className = "toast" + (isError ? " error" : "");
        item.textContent = message;
        wrap.appendChild(item);
        window.setTimeout(function () { item.remove(); }, 4200);
    }

    if (window.lucide && typeof lucide.createIcons === "function") {
        lucide.createIcons({ icons: lucide.icons });
    }
})();
