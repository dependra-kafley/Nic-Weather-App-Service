const districtDetailCard = document.getElementById("district-detail-card");
const districtDetailTitle = document.getElementById("district-detail-title");
const mapPlotTitle = document.getElementById("map-plot-title");
const nowcastLastUpdatedEl = document.getElementById("nowcast-last-updated");
const plotModeButtons = document.querySelectorAll(".plot-mode-btn[data-plot-mode]");
const districtMapContainer = document.getElementById("district-map-container");

const NO_DATA_FILL = "#cfe2ff";
const NO_DATA_STROKE = "#0d6efd";
const ACTIVE_EXTRA_STROKE = "#083b8a";

const PLOT_MODES = ["nowcast", "rainfall", "warning"];

let plotMode = "nowcast";
let nowcastLastRefreshedAt = null;
/** @type {Record<string, object>} */
let nowcastBySlug = {};
/** @type {Record<string, object>} */
let rainfallBySlug = {};
/** @type {Record<string, object>} */
let warningBySlug = {};
let districtNodes = [];
let districtUiIds = [];
let activeDistrictUiId = null;

const slugify = (value) => String(value || "")
    .trim()
    .replace(/_+/g, " ")
    .replace(/[^a-zA-Z0-9]+/g, "_")
    .replace(/^_+|_+$/g, "")
    .toUpperCase();

const toDisplayName = (districtUiId) => String(districtUiId || "")
    .replace(/_/g, " ")
    .replace(/\s+/g, " ")
    .trim();

const formatLastUpdated = (isoInstant) => {
    if (!isoInstant) {
        return "Not yet available";
    }
    try {
        return new Date(isoInstant).toLocaleString(undefined, {
            dateStyle: "medium",
            timeStyle: "short",
        });
    } catch {
        return "Not yet available";
    }
};

const rainfallCategoryLabel = (code) => {
    const normalized = String(code || "").trim().toUpperCase();
    switch (normalized) {
        case "LE":
            return "Large excess (≥60%)";
        case "E":
            return "Excess (20% to 59%)";
        case "N":
            return "Normal (-19% to 19%)";
        case "D":
            return "Deficient (-59% to -20%)";
        case "LD":
            return "Large deficient (-99% to -60%)";
        case "NR":
            return "No rain (-100%)";
        case "ND":
            return "No data";
        case "—":
            return "No data";
        default:
            return code || "No data";
    }
};

const nowcastStrokeForColorCode = (colorCode) => {
    switch (String(colorCode || "").trim()) {
        case "1":
            return "#004d00";
        case "2":
            return "#8a8a00";
        case "3":
            return "#a0522d";
        case "4":
            return "#7a0000";
        default:
            return NO_DATA_STROKE;
    }
};

const warningStrokeForColorCode = (colorCode) => {
    switch (String(colorCode || "").trim()) {
        case "1":
            return "#7f0000";
        case "2":
            return "#a0522d";
        case "3":
            return "#8a8a00";
        case "4":
            return "#15803d";
        default:
            return NO_DATA_STROKE;
    }
};

const rainfallStrokeForCategory = (category) => {
    const code = String(category || "").trim().toUpperCase();
    switch (code) {
        case "LE":
            return "#1e3a8a";
        case "E":
            return "#15803d";
        case "N":
            return "#ca8a04";
        case "D":
            return "#c2410c";
        case "LD":
            return "#b91c1c";
        case "NR":
            return "#450a0a";
        default:
            return NO_DATA_STROKE;
    }
};

const districtPathStyle = (districtUiId, active) => {
    if (plotMode === "rainfall") {
        const rf = rainfallBySlug[districtUiId];
        const fill = rf ? rf.colorHex : NO_DATA_FILL;
        const strokeInactive = rf ? rainfallStrokeForCategory(rf.dailyCategory) : NO_DATA_STROKE;
        const stroke = active ? ACTIVE_EXTRA_STROKE : strokeInactive;
        return `fill:${fill};stroke:${stroke};stroke-width:${active ? 3 : 2};cursor:pointer;transition:all .2s ease-in-out;`;
    }
    if (plotMode === "warning") {
        const w = warningBySlug[districtUiId];
        const fill = w ? w.colorHex : NO_DATA_FILL;
        const strokeInactive = w ? warningStrokeForColorCode(w.mapColor) : NO_DATA_STROKE;
        const stroke = active ? ACTIVE_EXTRA_STROKE : strokeInactive;
        return `fill:${fill};stroke:${stroke};stroke-width:${active ? 3 : 2};cursor:pointer;transition:all .2s ease-in-out;`;
    }
    const nc = nowcastBySlug[districtUiId];
    const fill = nc ? nc.colorHex : NO_DATA_FILL;
    const strokeInactive = nc ? nowcastStrokeForColorCode(nc.color) : NO_DATA_STROKE;
    const stroke = active ? ACTIVE_EXTRA_STROKE : strokeInactive;
    return `fill:${fill};stroke:${stroke};stroke-width:${active ? 3 : 2};cursor:pointer;transition:all .2s ease-in-out;`;
};

const ensureTooltipNode = (node) => {
    let titleNode = node.querySelector("title");
    if (!titleNode) {
        titleNode = document.createElementNS("http://www.w3.org/2000/svg", "title");
        node.appendChild(titleNode);
    }
    return titleNode;
};

const setDistrictNodeState = (node, active) => {
    const districtUiId = node.dataset.uiDistrictId;
    node.setAttribute("style", districtPathStyle(districtUiId, active));
};

const activateDistrict = (districtUiId) => {
    activeDistrictUiId = districtUiId;
    districtNodes.forEach((node) => {
        setDistrictNodeState(node, node.dataset.uiDistrictId === districtUiId);
    });
};

const applyDistrictTooltips = () => {
    districtNodes.forEach((node) => {
        const districtUiId = node.dataset.uiDistrictId;
        const name = toDisplayName(districtUiId);
        if (plotMode === "rainfall") {
            const rf = rainfallBySlug[districtUiId];
            ensureTooltipNode(node).textContent = rf
                ? `${name}: ${rf.dailyActual} mm`
                : name;
            return;
        }
        if (plotMode === "warning") {
            const w = warningBySlug[districtUiId];
            const day1 = w?.days?.[0];
            ensureTooltipNode(node).textContent = day1?.warningSummary
                ? `${name}: ${day1.warningSummary}`
                : name;
            return;
        }
        const nc = nowcastBySlug[districtUiId];
        ensureTooltipNode(node).textContent = nc?.nowcastSummary
            ? `${name}: ${nc.nowcastSummary}`
            : name;
    });
};

const nowcastLastUpdatedBlock = () => {
    if (!nowcastLastRefreshedAt) {
        return "";
    }
    return `<div class="nowcast-last-updated-card mb-2">
        <span class="nowcast-last-updated-label">Last updated</span>
        <span class="nowcast-last-updated-value">${formatLastUpdated(nowcastLastRefreshedAt)}</span>
    </div>`;
};

const buildNowcastDetailHtml = (districtUiId) => {
    const nc = nowcastBySlug[districtUiId];
    if (!nc) {
        return `<span class="text-secondary">No nowcast data for ${toDisplayName(districtUiId)}.</span>`;
    }
    const validWindow = [nc.toi, nc.vupto].filter((v) => v && v !== "—").join(" – ");
    return `
        <h3 class="h6 mb-2">${toDisplayName(districtUiId)}</h3>
        <p class="small text-uppercase text-secondary mb-2">District nowcast</p>
        ${nowcastLastUpdatedBlock()}
        <p><strong>Warning date:</strong> ${nc.date || "—"}</p>
        ${validWindow ? `<p><strong>Valid:</strong> ${validWindow}</p>` : ""}
        <p class="mb-0"><strong>Forecast:</strong> ${nc.nowcastSummary || nc.message || "—"}</p>
    `;
};

const buildRainfallDetailHtml = (districtUiId) => {
    const rf = rainfallBySlug[districtUiId];
    if (!rf) {
        return `<span class="text-secondary">No rainfall data for ${toDisplayName(districtUiId)}.</span>`;
    }
    const dailyLabel = rf.dailyCategoryLabel || rainfallCategoryLabel(rf.dailyCategory);
    return `
        <h3 class="h6 mb-2">${toDisplayName(districtUiId)}</h3>
        <p class="small text-uppercase text-secondary mb-2">District rainfall</p>
        <p><strong>Date:</strong> ${rf.date}</p>
        <div class="detail-section">
            <p class="fw-semibold mb-1">Daily</p>
            <p><strong>Actual:</strong> ${rf.dailyActual} mm &nbsp; <strong>Normal:</strong> ${rf.dailyNormal} mm</p>
            <p class="mb-0"><strong>Departure:</strong> ${rf.dailyDeparturePer} &nbsp; <strong>Status:</strong> ${dailyLabel}</p>
        </div>
        <div class="detail-section">
            <p class="fw-semibold mb-1">Weekly (${rf.weekDate})</p>
            <p><strong>Actual:</strong> ${rf.weeklyActual} mm &nbsp; <strong>Normal:</strong> ${rf.weeklyNormal} mm</p>
            <p class="mb-0"><strong>Departure:</strong> ${rf.weeklyDeparturePer} &nbsp; <strong>Status:</strong> ${rainfallCategoryLabel(rf.weeklyCategory)}</p>
        </div>
        <div class="detail-section">
            <p class="fw-semibold mb-1">Cumulative (from ${rf.cumulativeDate})</p>
            <p><strong>Actual:</strong> ${rf.cumulativeActual} mm &nbsp; <strong>Normal:</strong> ${rf.cumulativeNormal} mm</p>
            <p class="mb-0"><strong>Departure:</strong> ${rf.cumulativeDeparturePer} &nbsp; <strong>Status:</strong> ${rainfallCategoryLabel(rf.cumulativeCategory)}</p>
        </div>
        <div class="detail-section mb-0">
            <p class="fw-semibold mb-1">Monthly (${rf.monthlyDate})</p>
            <p><strong>Actual:</strong> ${rf.monthlyActual} mm &nbsp; <strong>Normal:</strong> ${rf.monthlyNormal} mm</p>
            <p class="mb-0"><strong>Departure:</strong> ${rf.monthlyDeparturePer} &nbsp; <strong>Status:</strong> ${rainfallCategoryLabel(rf.monthlyCategory)}</p>
        </div>
    `;
};

const buildWarningDetailHtml = (districtUiId) => {
    const w = warningBySlug[districtUiId];
    if (!w) {
        return `<span class="text-secondary">No district warning data for ${toDisplayName(districtUiId)}.</span>`;
    }
    const dayRows = (w.days || []).map((day) => `
        <div class="warning-day-row">
            <div class="fw-medium warning-day-date">${day.label}</div>
            <div class="warning-day-text">${day.warningSummary || "No warning"}</div>
        </div>
    `).join("");
    return `
        <h3 class="h6 mb-2">${toDisplayName(districtUiId)}</h3>
        <p class="small text-uppercase text-secondary mb-2">District warning</p>
        <p><strong>District:</strong> ${w.district}</p>
        <p class="mb-3"><strong>Date:</strong> ${w.date}</p>
        <div class="warning-days-list">${dayRows}</div>
    `;
};

const updateDistrictDetailCard = (districtUiId) => {
    if (!districtUiId) {
        districtDetailCard.innerHTML = defaultHoverHint();
        return;
    }
    if (plotMode === "rainfall") {
        districtDetailCard.innerHTML = buildRainfallDetailHtml(districtUiId);
    } else if (plotMode === "warning") {
        districtDetailCard.innerHTML = buildWarningDetailHtml(districtUiId);
    } else {
        districtDetailCard.innerHTML = buildNowcastDetailHtml(districtUiId);
    }
};

const defaultHoverHint = () => {
    if (plotMode === "nowcast" && nowcastLastRefreshedAt) {
        return `${nowcastLastUpdatedBlock()}<span class="text-secondary">Hover over a district to view nowcast details.</span>`;
    }
    if (plotMode === "rainfall") {
        return "Hover over a district to view rainfall details.";
    }
    if (plotMode === "warning") {
        return "Hover over a district to view district warning details.";
    }
    return "Hover over a district to view nowcast details.";
};

const updateNowcastLastUpdatedBanner = () => {
    if (!nowcastLastUpdatedEl) {
        return;
    }
    const show = plotMode === "nowcast";
    nowcastLastUpdatedEl.classList.toggle("d-none", !show);
    if (!show) {
        nowcastLastUpdatedEl.innerHTML = "";
        return;
    }
    if (nowcastLastRefreshedAt) {
        nowcastLastUpdatedEl.innerHTML = `
            <span class="nowcast-last-updated-label">Last updated</span>
            <span class="nowcast-last-updated-value">${formatLastUpdated(nowcastLastRefreshedAt)}</span>`;
    } else {
        nowcastLastUpdatedEl.innerHTML = `<span class="nowcast-last-updated-label">Last updated</span>
            <span class="nowcast-last-updated-value text-muted">Not yet available</span>`;
    }
};

const updatePlotChrome = () => {
    const isRainfall = plotMode === "rainfall";
    const isWarning = plotMode === "warning";

    mapPlotTitle.textContent = isRainfall ? "Rainfall" : isWarning ? "Warning" : "Nowcast";
    districtDetailTitle.textContent = isRainfall
        ? "Rainfall details"
        : isWarning
            ? "District warning details"
            : "Nowcast details";

    updateNowcastLastUpdatedBanner();

    plotModeButtons.forEach((btn) => {
        const active = btn.dataset.plotMode === plotMode;
        btn.classList.toggle("active", active);
        btn.setAttribute("aria-selected", String(active));
    });
};

const indexByDistrictKey = (payload) => payload.reduce((acc, row) => {
    if (row.districtKey) {
        acc[row.districtKey] = row;
    }
    return acc;
}, {});

const loadSyncStatus = async () => {
    try {
        const response = await fetch("/api/weather/sync-status");
        if (!response.ok) {
            return;
        }
        const data = await response.json();
        nowcastLastRefreshedAt = data.nowcastLastRefreshedAt || null;
    } catch {
        nowcastLastRefreshedAt = null;
    }
    updateNowcastLastUpdatedBanner();
    if (plotMode === "nowcast" && activeDistrictUiId) {
        updateDistrictDetailCard(activeDistrictUiId);
    }
};

const loadNowcastMapData = async () => {
    try {
        const response = await fetch("/api/weather/nowcast/map");
        nowcastBySlug = response.ok ? indexByDistrictKey(await response.json()) : {};
    } catch {
        nowcastBySlug = {};
    }
};

const loadRainfallMapData = async () => {
    try {
        const response = await fetch("/api/weather/rainfall/map");
        rainfallBySlug = response.ok ? indexByDistrictKey(await response.json()) : {};
    } catch {
        rainfallBySlug = {};
    }
};

const loadWarningMapData = async () => {
    try {
        const response = await fetch("/api/weather/district-warning/map");
        warningBySlug = response.ok ? indexByDistrictKey(await response.json()) : {};
    } catch {
        warningBySlug = {};
    }
};

const repaintAllDistrictPaths = () => {
    districtNodes.forEach((node) => {
        setDistrictNodeState(node, node.dataset.uiDistrictId === activeDistrictUiId);
    });
};

const setPlotMode = (mode) => {
    if (!PLOT_MODES.includes(mode)) {
        return;
    }
    plotMode = mode;
    updatePlotChrome();
    repaintAllDistrictPaths();
    applyDistrictTooltips();
    if (activeDistrictUiId) {
        updateDistrictDetailCard(activeDistrictUiId);
    } else {
        districtDetailCard.innerHTML = defaultHoverHint();
    }
};

const wirePlotModeMenu = () => {
    plotModeButtons.forEach((button) => {
        button.addEventListener("click", () => {
            document.getElementById("dashboard-map-view")?.classList.remove("d-none");
            document.getElementById("dashboard-aws-view")?.classList.add("d-none");
            document.getElementById("rainfall-observed-btn")?.classList.remove("active");
            setPlotMode(button.dataset.plotMode);
            document.getElementById("map-panel")?.scrollIntoView({behavior: "smooth", block: "start"});
        });
    });
};

const wireMapHover = () => {
    districtNodes.forEach((districtNode) => {
        districtNode.addEventListener("mouseenter", () => {
            const districtUiId = districtNode.dataset.uiDistrictId;
            activateDistrict(districtUiId);
            updateDistrictDetailCard(districtUiId);
        });
    });
};

const injectInlineSvg = async () => {
    const response = await fetch("/meghalaya_districts.svg");
    const svgMarkup = await response.text();
    const parser = new DOMParser();
    const svgDoc = parser.parseFromString(svgMarkup, "image/svg+xml");
    const svgElement = svgDoc.querySelector("svg");
    if (!svgElement) {
        throw new Error("SVG not found");
    }
    svgElement.setAttribute("id", "district-map");
    svgElement.setAttribute("role", "img");
    svgElement.setAttribute("aria-label", "District weather map");
    districtMapContainer.innerHTML = "";
    districtMapContainer.appendChild(svgElement);
};

const setupDistrictNodes = () => {
    districtNodes = Array.from(document.querySelectorAll("#district-map path[id]"));
    districtNodes = districtNodes.filter((node) => node.id && node.id !== "mesh_polyfill");

    districtNodes.forEach((node) => {
        const districtUiId = slugify(node.id);
        node.dataset.uiDistrictId = districtUiId;
        setDistrictNodeState(node, false);
    });

    districtUiIds = [...new Set(districtNodes.map((node) => node.dataset.uiDistrictId))];
};

const init = async () => {
    try {
        wirePlotModeMenu();
        updatePlotChrome();
        await injectInlineSvg();
        setupDistrictNodes();
        wireMapHover();
        await Promise.all([
            loadSyncStatus(),
            loadNowcastMapData(),
            loadRainfallMapData(),
            loadWarningMapData(),
        ]);
        repaintAllDistrictPaths();
        applyDistrictTooltips();

        const preferredUiId = "SOUTH_GARO_HILLS";
        const firstDistrictUiId = districtUiIds.includes(preferredUiId)
            ? preferredUiId
            : districtUiIds[0];
        if (firstDistrictUiId) {
            activateDistrict(firstDistrictUiId);
            updateDistrictDetailCard(firstDistrictUiId);
        }
    } catch {
        districtDetailCard.innerHTML = `<span class="text-danger">Could not load map data.</span>`;
    }
};

init();
