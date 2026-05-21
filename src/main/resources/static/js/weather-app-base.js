/**
 * Resolves app-relative URLs when deployed under a servlet context path (e.g. /weather).
 * Set window.WEATHER_APP_BASE from the dashboard page before loading other scripts.
 */
window.weatherAppUrl = function (path) {
    const base = window.WEATHER_APP_BASE || "/";
    const relative = path.replace(/^\//, "");
    const root = base.endsWith("/") ? base : `${base}/`;
    return new URL(relative, root).href;
};
