/**
 * Rainfall observed tables — scoped IIFE avoids clashing with escapeHtml in city-weather.js.
 */
(() => {
    const AWS_TYPE_LABEL = { ORG: "ORG", AWS: "AWS", ARG: "ARG" };

    const escapeHtml = (value) => String(value ?? "")
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");

    const formatDistrict = (d) => {
        if (!d || d === "—") {
            return "—";
        }
        return escapeHtml(String(d).replace(/_/g, " "));
    };

    const awsField = (s, upper, camel) => s[camel] ?? s[upper] ?? "";

    const rowHtml = (station, typeKey) => {
        const type = AWS_TYPE_LABEL[typeKey] || typeKey;
        const stName = awsField(station, "STATION", "station");
        const district = awsField(station, "DISTRICT", "district");
        const date = awsField(station, "DATE", "date");
        const time = awsField(station, "TIME", "time");
        const curr = awsField(station, "CURR_TEMP", "currTemp");
        const minT = awsField(station, "MIN_TEMP", "minTemp");
        const maxT = awsField(station, "MAX_TEMP", "maxTemp");
        const wdir = awsField(station, "WIND_DIRECTION", "windDirection");
        const ws = awsField(station, "WIND_SPEED", "windSpeed");
        const msg = awsField(station, "WEATHER_MESSAGE", "weatherMessage");
        const sid = awsField(station, "ID", "id");
        const maxWind = ws != null && ws !== "" ? escapeHtml(String(ws)) : "—";
        return `
        <tr>
            <td class="fw-medium">${escapeHtml(stName || sid)}</td>
            <td class="aws-type-cell aws-type-${typeKey.toLowerCase()}">${type}</td>
            <td>${formatDistrict(district)}</td>
            <td>—</td>
            <td class="text-nowrap">${escapeHtml(date)}</td>
            <td class="text-nowrap">${escapeHtml(time)}</td>
            <td>${escapeHtml(curr)}</td>
            <td>${escapeHtml(minT)} / ${escapeHtml(maxT)}</td>
            <td>${escapeHtml(wdir)}</td>
            <td>${maxWind}</td>
            <td>${escapeHtml(msg)}</td>
        </tr>`;
    };

    const tableBlock = (title, typeKey, rows) => {
        if (!rows || rows.length === 0) {
            return `<p class="text-secondary small mb-4">No ${title} stations in cache yet.</p>`;
        }
        const body = rows.map((s) => rowHtml(s, typeKey)).join("");
        return `
        <h3 class="h6 mt-4 mb-2 aws-table-title aws-type-${typeKey.toLowerCase()}">${escapeHtml(title)}</h3>
        <div class="table-responsive mb-4">
            <table class="table table-sm table-bordered align-middle aws-station-table mb-0">
                <thead class="table-light small">
                    <tr>
                        <th>Station name</th>
                        <th>Type</th>
                        <th>District</th>
                        <th>Rainfall (mm)</th>
                        <th>Date</th>
                        <th>Time</th>
                        <th>Temp °C</th>
                        <th>Min / Max</th>
                        <th>Wind dir</th>
                        <th>Wind (kmph)</th>
                        <th>Weather</th>
                    </tr>
                </thead>
                <tbody>${body}</tbody>
            </table>
        </div>`;
    };

    const renderAwsObservations = (data) => {
        const root = document.getElementById("aws-observations-root");
        if (!root) {
            return;
        }
        const org = data.org || [];
        const aws = data.aws || [];
        const arg = data.arg || [];
        root.innerHTML =
            tableBlock("ORG — Ordinary rain gauge", "ORG", org)
            + tableBlock("AWS — Automatic weather stations", "AWS", aws)
            + tableBlock("ARG — Automatic rain gauge", "ARG", arg);
    };

    const loadAwsObservations = async () => {
        const root = document.getElementById("aws-observations-root");
        if (!root) {
            return;
        }
        try {
            const response = await fetch("/api/weather/aws-observations");
            if (!response.ok) {
                root.innerHTML = `<p class="text-danger mb-0">Could not load observations (${response.status}).</p>`;
                return;
            }
            const data = await response.json();
            renderAwsObservations(data);
        } catch {
            root.innerHTML = `<p class="text-danger mb-0">Could not load observations.</p>`;
        }
    };

    const wireRainfallObservedNav = () => {
        const mapView = document.getElementById("dashboard-map-view");
        const awsView = document.getElementById("dashboard-aws-view");
        const btn = document.getElementById("rainfall-observed-btn");
        const plotButtons = document.querySelectorAll(".plot-mode-btn[data-plot-mode]");

        if (!mapView || !awsView || !btn) {
            return;
        }

        btn.addEventListener("click", () => {
            mapView.classList.add("d-none");
            awsView.classList.remove("d-none");
            btn.classList.add("active");
            plotButtons.forEach((b) => {
                b.classList.remove("active");
                b.setAttribute("aria-selected", "false");
            });
            btn.setAttribute("aria-selected", "true");
            loadAwsObservations();
            awsView.scrollIntoView({ behavior: "smooth", block: "start" });
        });
    };

    wireRainfallObservedNav();
})();
