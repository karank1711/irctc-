(function () {
    const reduceMotion = window.matchMedia("(prefers-reduced-motion: reduce)").matches;
    revealWelcome(reduceMotion);
    const chartData = readJson("chart-data");
    const serviceData = readJson("service-data");
    let bookingChart;
    let serviceChart;

    document.querySelectorAll(".metric-value").forEach(function (element) {
        const finalText = element.dataset.final || element.textContent;
        const numeric = Number(element.dataset.value);
        if (reduceMotion || !Number.isFinite(numeric)) {
            element.textContent = finalText;
            return;
        }
        const observer = new IntersectionObserver(function (entries) {
            entries.forEach(function (entry) {
                if (!entry.isIntersecting || element.dataset.animated === "true") return;
                element.dataset.animated = "true";
                animate(element, numeric, finalText);
                observer.disconnect();
            });
        }, { threshold: 0.4 });
        observer.observe(element);
    });

    if (chartData && window.Chart) {
        bookingChart = drawLine("bookingChart", chartData);
        document.getElementById("periodSelect")?.addEventListener("change", function (event) {
            const period = event.target.value;
            document.getElementById("loadingBar")?.classList.add("show");
            fetch(contextPath() + "/api/dashboard/bookings?period=" + period, { headers: { "Accept": "application/json" } })
                .then(function (response) {
                    if (!response.ok) throw new Error("chart failed");
                    return response.json();
                })
                .then(function (data) {
                    bookingChart.data.labels = data.labels;
                    bookingChart.data.datasets[0].data = data.counts;
                    bookingChart.update();
                    const note = document.getElementById("chartEmpty");
                    if (note) note.hidden = !data.empty;
                })
                .catch(function () {
                    const note = document.getElementById("chartEmpty");
                    if (note) {
                        note.hidden = false;
                        note.textContent = "The chart could not be refreshed.";
                    }
                })
                .finally(function () { document.getElementById("loadingBar")?.classList.remove("show"); });
        });
    }
    if (serviceData && window.Chart && serviceData.length && serviceData.some(function (item) { return item.count > 0; })) {
        serviceChart = new Chart(document.getElementById("serviceChart"), {
            type: "doughnut",
            data: {
                labels: serviceData.map(function (item) { return item.label; }),
                datasets: [{
                    data: serviceData.map(function (item) { return item.count; }),
                    backgroundColor: serviceData.map(function (item) { return item.color; }),
                    borderWidth: 0
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                cutout: "68%",
                plugins: { legend: { display: false } },
                animation: reduceMotion ? false : { duration: 700 }
            }
        });
    }

    function drawLine(canvasId, data) {
        return new Chart(document.getElementById(canvasId), {
            type: "line",
            data: {
                labels: data.labels,
                datasets: [{
                    label: "Bookings",
                    data: data.counts,
                    borderColor: "#2563eb",
                    backgroundColor: "rgba(37, 99, 235, 0.12)",
                    fill: true,
                    tension: 0.35,
                    pointRadius: 3,
                    pointBackgroundColor: "#2563eb"
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: { legend: { display: false } },
                scales: {
                    x: { grid: { display: false }, ticks: { maxTicksLimit: 8 } },
                    y: { beginAtZero: true, ticks: { precision: 0 } }
                },
                animation: reduceMotion ? false : { duration: 800 }
            }
        });
    }

    function animate(element, target, finalText) {
        const started = performance.now();
        const duration = 700;
        function frame(now) {
            const progress = Math.min(1, (now - started) / duration);
            element.textContent = Math.round(target * progress).toLocaleString("en-IN");
            if (progress < 1) {
                requestAnimationFrame(frame);
            } else {
                element.textContent = finalText;
            }
        }
        requestAnimationFrame(frame);
    }

    function revealWelcome(reduce) {
        const hero = document.querySelector(".hero");
        if (!hero || reduce) return;
        const nodes = [".hero-kicker", ".hero h1", ".hero-lead", ".hero-status"].map(function (selector) {
            return hero.querySelector(selector);
        }).filter(Boolean);
        const stillRunning = nodes.some(function (node) {
            return node.getAnimations().some(function (item) { return item.playState === "running"; });
        });
        if (stillRunning) return;
        nodes.forEach(function (node) { node.style.animation = "none"; });
        void hero.offsetWidth;
        nodes.forEach(function (node) { node.style.animation = ""; });
    }

    window.addEventListener("pageshow", function (event) {
        if (event.persisted) revealWelcome(reduceMotion);
    });

    function readJson(id) {
        const node = document.getElementById(id);
        if (!node) return null;
        try { return JSON.parse(node.textContent); } catch (error) { return null; }
    }
    function contextPath() { return document.body.dataset.context || ""; }
})();
