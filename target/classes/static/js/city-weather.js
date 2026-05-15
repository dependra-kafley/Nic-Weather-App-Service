/**
 * Colored inline SVGs (emoji render monochrome on some Windows setups).
 */
const WEATHER_ICONS = {
    clear: `<svg class="city-weather-weather-svg" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 48 48" aria-hidden="true">
        <circle cx="24" cy="24" r="9" fill="#fbbf24"/>
        <g stroke="#f59e0b" stroke-width="2.5" stroke-linecap="round">
            <path d="M24 5v5M24 38v5M5 24h5M38 24h5M10.5 10.5l3.5 3.5M34 34l3.5 3.5M37.5 10.5L34 14M14 34l-3.5 3.5"/>
        </g>
    </svg>`,
    cloudy: `<svg class="city-weather-weather-svg" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 48 48" aria-hidden="true">
        <path fill="#94a3b8" d="M10 28c0-7 6-13 14-13 2.5 0 4.8.6 6.8 1.7C33 12 38 10 43 12c5 2 7 7 5.5 12 2.5.8 4.5 3 4.5 6v2H10v-2z"/>
    </svg>`,
    rain: `<svg class="city-weather-weather-svg" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 48 48" aria-hidden="true">
        <path fill="#94a3b8" d="M10 24c0-7 6-13 14-13s14 6 14 13v3H10v-3z"/>
        <path stroke="#0ea5e9" stroke-width="3" stroke-linecap="round" fill="none" d="M17 30v9M24 28v11M31 30v9"/>
    </svg>`,
    thunder: `<svg class="city-weather-weather-svg" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 48 48" aria-hidden="true">
        <path fill="#64748b" d="M10 24c0-7 6-13 14-13s14 6 14 13v3H10v-3z"/>
        <path fill="#fde047" stroke="#ca8a04" stroke-width="0.8" d="M22 9L12 28h9l-5 15 20-22h-9l5-12z"/>
    </svg>`,
    fog: `<svg class="city-weather-weather-svg" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 48 48" aria-hidden="true">
        <path stroke="#cbd5e1" stroke-width="4" stroke-linecap="round" d="M6 16h36M4 24h34M8 32h30"/>
        <path stroke="#e2e8f0" stroke-width="3" stroke-linecap="round" d="M10 20h22" opacity="0.9"/>
    </svg>`,
    mixed: `<svg class="city-weather-weather-svg" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 48 48" aria-hidden="true">
        <circle cx="32" cy="17" r="7" fill="#fcd34d"/>
        <path fill="#94a3b8" d="M6 30c0-5.5 4.5-10 10-10 2 0 3.8.6 5.3 1.6C24 17 29 16 33 18c3.5 1.8 5.7 5.4 5.7 9.4V32H6v-1.6z"/>
    </svg>`
};

const weatherIconMarkup = (token) => WEATHER_ICONS[token] || WEATHER_ICONS.mixed;

const escapeHtml = (value) => String(value ?? "")
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&#039;");

const formatTemp = (value) => {
    if (value == null || value === "—") {
        return "—";
    }
    const s = String(value);
    if (s.includes("°")) {
        return escapeHtml(s);
    }
    return `${escapeHtml(s)}°C`;
};

const buildDayRows = (days) => days.map((day) => `
    <tr>
        <td class="text-nowrap fw-medium">${escapeHtml(day.label)}</td>
        <td class="text-center city-weather-sky-cell align-middle" title="${escapeHtml(day.forecastText)}">${weatherIconMarkup(day.iconToken)}</td>
        <td class="text-nowrap">${formatTemp(day.maxTemp)}</td>
        <td class="text-nowrap">${formatTemp(day.minTemp)}</td>
        <td class="small text-secondary">${escapeHtml(day.forecastText)}</td>
    </tr>
`).join("");

const buildPanel = (item, index) => {
    const day1 = item.days[0] || {
        label: "Today",
        maxTemp: "—",
        minTemp: "—",
        forecastText: "",
        iconToken: "mixed"
    };
    const panelId = `city-weather-panel-${item.stationCode || index}`;
    const summaryMax = formatTemp(day1.maxTemp);
    const summaryMin = formatTemp(day1.minTemp);

    return `
<div class="city-weather-panel" id="${panelId}" role="listitem" data-expanded="false">
    <button type="button" class="city-weather-header w-100 text-start border-0 bg-transparent d-flex align-items-start"
            aria-expanded="false" aria-controls="${panelId}-body">
        <span class="city-weather-icon lh-1 pt-1" aria-hidden="true">${weatherIconMarkup(day1.iconToken)}</span>
        <div class="city-weather-header-text flex-grow-1 min-w-0">
            <div class="city-weather-station-name text-truncate">${escapeHtml(item.stationName)}</div>
            <div class="city-weather-summary text-muted" aria-label="Today's temperature range">
                <div class="city-weather-temp-line">Max <span class="city-weather-temp-value">${summaryMax}</span></div>
                <div class="city-weather-temp-line">Min <span class="city-weather-temp-value">${summaryMin}</span></div>
            </div>
        </div>
        <span class="city-weather-chevron text-muted pt-1" aria-hidden="true">▼</span>
    </button>
    <div class="city-weather-body" id="${panelId}-body">
        <div class="city-weather-meta px-3 pb-2">
            <span class="me-2"><strong>Date:</strong> ${escapeHtml(item.observationDate)}</span>
            <span class="me-2"><strong>Rain 24h:</strong> ${escapeHtml(item.past24HrsRainfall)} mm</span>
            <span class="me-2"><strong>RH 0830:</strong> ${escapeHtml(item.relativeHumidityAt0830)}%</span>
            <span><strong>Sun:</strong> ${escapeHtml(item.sunriseTime)} / ${escapeHtml(item.sunsetTime)}</span>
        </div>
        <div class="table-responsive px-2 pb-2">
            <table class="table table-sm table-borderless city-weather-table mb-0">
                <thead class="small text-secondary">
                    <tr>
                        <th>Day</th>
                        <th class="text-center">Sky</th>
                        <th>Max</th>
                        <th>Min</th>
                        <th>Forecast</th>
                    </tr>
                </thead>
                <tbody>
                    ${buildDayRows(item.days)}
                </tbody>
            </table>
        </div>
    </div>
</div>`;
};

const collapseAllPanels = (root) => {
    root.querySelectorAll(".city-weather-panel").forEach((panel) => {
        panel.classList.remove("expanded");
        panel.dataset.expanded = "false";
        const btn = panel.querySelector(".city-weather-header");
        if (btn) {
            btn.setAttribute("aria-expanded", "false");
        }
    });
};

const wireAccordion = (root) => {
    root.addEventListener("click", (event) => {
        const header = event.target.closest(".city-weather-header");
        if (!header) {
            return;
        }
        const panel = header.closest(".city-weather-panel");
        if (!panel) {
            return;
        }
        const isOpen = panel.dataset.expanded === "true";
        collapseAllPanels(root);
        if (!isOpen) {
            panel.classList.add("expanded");
            panel.dataset.expanded = "true";
            header.setAttribute("aria-expanded", "true");
        }
    });
};

const initCityWeatherAccordion = async () => {
    const root = document.getElementById("city-weather-accordion-root");
    if (!root) {
        return;
    }

    try {
        const response = await fetch("/api/weather/cities");
        if (!response.ok) {
            root.innerHTML = `<p class="small text-danger mb-0 py-2">City weather could not be loaded (${response.status}).</p>`;
            return;
        }
        const items = await response.json();
        if (!items.length) {
            root.innerHTML = `<p class="small text-secondary mb-0 py-2">No city weather data returned. Check IMD connectivity or enable mock data.</p>`;
            return;
        }
        root.innerHTML = items.map((item, index) => buildPanel(item, index)).join("");
        wireAccordion(root);
    } catch {
        root.innerHTML = `<p class="small text-danger mb-0 py-2">City weather could not be loaded.</p>`;
    }
};

initCityWeatherAccordion();
