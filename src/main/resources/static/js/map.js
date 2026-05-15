const districtDetailCard = document.getElementById("district-detail-card");
const districtDetailTitle = document.getElementById("district-detail-title");
const mapPlotTitle = document.getElementById("map-plot-title");
const plotModeButtons = document.querySelectorAll(".plot-mode-btn");
const rainfallLegend = document.getElementById("rainfall-legend");
const nowcastLegend = document.getElementById("nowcast-legend");
const warningLegend = document.getElementById("warning-legend");
const districtMapContainer = document.getElementById("district-map-container");

const NO_DATA_FILL = "#cfe2ff";
const NO_DATA_STROKE = "#0d6efd";
const ACTIVE_EXTRA_STROKE = "#083b8a";

const PLOT_MODES = ["nowcast", "rainfall", "warning"];

let plotMode = "nowcast";
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
                ? `${name}: ${rf.dailyActual} mm (${rf.dailyCategory})`
                : `${name}: No rainfall data`;
            return;
        }
        if (plotMode === "warning") {
            const w = warningBySlug[districtUiId];
            ensureTooltipNode(node).textContent = w
                ? `${name}: Day 1 colour ${w.mapColor}`
                : `${name}: No district warning data`;
            return;
        }
        const nc = nowcastBySlug[districtUiId];
        ensureTooltipNode(node).textContent = nc
            ? `${name}: nowcast level ${nc.color}`
            : `${name}: No nowcast data`;
    });
};

const buildNowcastDetailHtml = (districtUiId) => {
    const nc = nowcastBySlug[districtUiId];
    if (!nc) {
        return `<span class="text-secondary">No IMD nowcast data for ${toDisplayName(districtUiId)}.</span>`;
    }
    return `
        <h3 class="h6 mb-2">${toDisplayName(districtUiId)}</h3>
        <p class="small text-uppercase text-secondary mb-2">IMD district nowcast</p>
        <p><strong>Obj id:</strong> ${nc.objId}</p>
        <p><strong>District:</strong> ${nc.stateDistrict}</p>
        <p><strong>Warning date:</strong> ${nc.date}</p>
        <p><strong>Issued (toi):</strong> ${nc.toi} &nbsp; <strong>Valid till:</strong> ${nc.vupto}</p>
        <p><strong>Summary:</strong> ${nc.nowcastSummary}</p>
        <p class="mt-2 mb-0"><strong>Colour code:</strong> ${nc.color}
            <span style="display:inline-block;width:14px;height:14px;border-radius:3px;vertical-align:middle;border:1px solid #333;background:${nc.colorHex}"></span>
        </p>
    `;
};

const buildRainfallDetailHtml = (districtUiId) => {
    const rf = rainfallBySlug[districtUiId];
    if (!rf) {
        return `<span class="text-secondary">No IMD rainfall data for ${toDisplayName(districtUiId)}.</span>`;
    }
    return `
        <h3 class="h6 mb-2">${toDisplayName(districtUiId)}</h3>
        <p class="small text-uppercase text-secondary mb-2">IMD district rainfall</p>
        <p><strong>Obj id:</strong> ${rf.objId}</p>
        <p><strong>Date:</strong> ${rf.date}</p>
        <hr class="my-2">
        <p class="fw-semibold mb-1">Daily</p>
        <p><strong>Actual:</strong> ${rf.dailyActual} mm &nbsp; <strong>Normal:</strong> ${rf.dailyNormal} mm</p>
        <p><strong>Departure:</strong> ${rf.dailyDeparturePer} &nbsp; <strong>Category:</strong> ${rf.dailyCategory} (${rf.dailyCategoryLabel})</p>
        <span style="display:inline-block;width:14px;height:14px;border-radius:3px;vertical-align:middle;border:1px solid #333;background:${rf.colorHex}"></span>
        <hr class="my-2">
        <p class="fw-semibold mb-1">Weekly (${rf.weekDate})</p>
        <p><strong>Actual:</strong> ${rf.weeklyActual} mm &nbsp; <strong>Normal:</strong> ${rf.weeklyNormal} mm</p>
        <p><strong>Departure:</strong> ${rf.weeklyDeparturePer} &nbsp; <strong>Category:</strong> ${rf.weeklyCategory}</p>
        <hr class="my-2">
        <p class="fw-semibold mb-1">Cumulative (from ${rf.cumulativeDate})</p>
        <p><strong>Actual:</strong> ${rf.cumulativeActual} mm &nbsp; <strong>Normal:</strong> ${rf.cumulativeNormal} mm</p>
        <p><strong>Departure:</strong> ${rf.cumulativeDeparturePer} &nbsp; <strong>Category:</strong> ${rf.cumulativeCategory}</p>
        <hr class="my-2">
        <p class="fw-semibold mb-1">Monthly (${rf.monthlyDate})</p>
        <p><strong>Actual:</strong> ${rf.monthlyActual} mm &nbsp; <strong>Normal:</strong> ${rf.monthlyNormal} mm</p>
        <p class="mb-0"><strong>Departure:</strong> ${rf.monthlyDeparturePer} &nbsp; <strong>Category:</strong> ${rf.monthlyCategory}</p>
    `;
};

const buildWarningDetailHtml = (districtUiId) => {
    const w = warningBySlug[districtUiId];
    if (!w) {
        return `<span class="text-secondary">No IMD district warning data for ${toDisplayName(districtUiId)}.</span>`;
    }
    const dayRows = (w.days || []).map((day) => `
        <tr>
            <td class="fw-medium">${day.label}</td>
            <td class="text-center">
                <span style="display:inline-block;width:12px;height:12px;border-radius:2px;border:1px solid #333;background:${day.colorHex}"></span>
                ${day.color}
            </td>
            <td>${day.warningCodes}</td>
            <td class="small text-secondary">${day.warningSummary}</td>
        </tr>
    `).join("");
    return `
        <h3 class="h6 mb-2">${toDisplayName(districtUiId)}</h3>
        <p class="small text-uppercase text-secondary mb-2">IMD district wise warning</p>
        <p><strong>Obj id:</strong> ${w.objId}</p>
        <p><strong>District:</strong> ${w.district}</p>
        <p><strong>Date:</strong> ${w.date} &nbsp; <strong>UTC:</strong> ${w.utc}</p>
        <p><strong>Map colour (Day 1):</strong> ${w.mapColor}
            <span style="display:inline-block;width:14px;height:14px;border-radius:3px;vertical-align:middle;border:1px solid #333;background:${w.colorHex}"></span>
        </p>
        <div class="table-responsive mt-2">
            <table class="table table-sm table-borderless mb-0">
                <thead class="small text-secondary">
                    <tr>
                        <th>Day</th>
                        <th>Colour</th>
                        <th>Codes</th>
                        <th>Warning</th>
                    </tr>
                </thead>
                <tbody>${dayRows}</tbody>
            </table>
        </div>
    `;
};

const updateDistrictDetailCard = (districtUiId) => {
    if (!districtUiId) {
        districtDetailCard.innerHTML = "Hover over a district on the map to view details.";
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
    if (plotMode === "rainfall") {
        return "Hover over a district to view rainfall details.";
    }
    if (plotMode === "warning") {
        return "Hover over a district to view district warning details.";
    }
    return "Hover over a district to view nowcast details.";
};

const updatePlotChrome = () => {
    const isRainfall = plotMode === "rainfall";
    const isWarning = plotMode === "warning";
    const isNowcast = plotMode === "nowcast";

    mapPlotTitle.textContent = isRainfall
        ? "Rainfall warning plot"
        : isWarning
            ? "District wise warning"
            : "Nowcast plot";

    districtDetailTitle.textContent = isRainfall
        ? "Rainfall details"
        : isWarning
            ? "District warning details"
            : "Nowcast details";

    rainfallLegend.classList.toggle("d-none", !isRainfall);
    rainfallLegend.setAttribute("aria-hidden", String(!isRainfall));

    warningLegend.classList.toggle("d-none", !isWarning);
    warningLegend.setAttribute("aria-hidden", String(!isWarning));

    nowcastLegend.classList.toggle("d-none", !isNowcast);
    nowcastLegend.setAttribute("aria-hidden", String(!isNowcast));

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
        await Promise.all([loadNowcastMapData(), loadRainfallMapData(), loadWarningMapData()]);
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
