# Deployment Guide — NIC Weather App Service

This document describes how to build, configure, and run **Nic-Weather-App-Service** in production. The app is a Spring Boot 3.3 service (Java 17) that caches IMD weather data in an on-disk **H2** database and serves a Thymeleaf dashboard plus JSON APIs.

---

## Prerequisites

| Requirement | Notes |
|-------------|--------|
| **JDK 17** | Required at build and runtime |
| **Maven 3.9+** | To build the WAR (or use a pre-built artifact from CI) |
| **Tomcat 10.1+** (WAR deploy) | Servlet 6 / Jakarta EE 9 — required for Spring Boot 3 |
| **Outbound HTTPS** | Hosts must reach `mausam.imd.gov.in` and `city.imd.gov.in` when using the `prod` profile |
| **Disk** | Writable directory for H2 files (default `./data/`) |
| **Time zone** | Server clock should be correct; schedulers use the JVM default time zone (typically set the OS to `Asia/Kolkata` for 9 AM daily jobs) |

---

## Quick production deploy (WAR on Tomcat)

### 1. Build (prod baked into the WAR)

```bash
cd Nic-Weather-App-Service
mvn -q -Pprod -DskipTests clean package
```

Use **`-Pprod`** so the WAR ships with the **`prod`** Spring profile (live IMD APIs, Thymeleaf cache, no H2 console). You do **not** need `SPRING_PROFILES_ACTIVE` on Tomcat.

| Maven profile | Command | Use |
|---------------|---------|-----|
| **`prod`** | `mvn -Pprod package` | **Production WAR** → `target/weather.war` |
| **`local`** (default) | `mvn package` or `mvn spring-boot:run` | Dev / mock IMD data |

Artifact: **`target/weather.war`**

### 2. Prepare data directory

On the server (writable by the Tomcat user), e.g. `/var/lib/nic-weather/`:

```bash
mkdir -p /var/lib/nic-weather/data
```

H2 creates `weather-db.mv.db` under the path you set in `WEATHER_DB_PATH`. **Back up that directory** before upgrades.

### 3. Deploy to Tomcat

1. Copy `target/weather.war` to Tomcat’s `webapps/` folder (the WAR name **`weather`** gives context path **`/weather`**).
2. **No Tomcat env vars required** if you built with **`-Pprod`**:
   - Spring profile **`prod`** is inside the WAR (`application.properties`).
   - H2 database defaults to **`${catalina.base}/weather-data/weather-db`** (under your Tomcat install). Ensure the Tomcat user can write there.
3. Start or restart Tomcat.

Optional overrides (only if needed): `WEATHER_DB_PATH`, `SPRING_PROFILES_ACTIVE`, `SERVER_SERVLET_CONTEXT_PATH`.

**Do not** set `SERVER_SERVLET_CONTEXT_PATH=/weather` when using `weather.war` on Tomcat — the context path already comes from the WAR file name.

### 4. Verify

| Check | URL (Tomcat on port 8080) |
|-------|---------------------------|
| Dashboard | `http://<host>:8080/weather/` |
| Health | `http://<host>:8080/weather/actuator/health` |
| Sync status | `http://<host>:8080/weather/api/weather/sync-status` |

Behind HTTPS at `https://relief.megrevenuedm.gov.in/weather/`, the reverse proxy should forward to Tomcat’s `/weather/` context.

On first start the app loads any existing H2 snapshots, then refreshes all IMD APIs in the background. Allow 1–2 minutes before expecting a full map.

### Optional: run prod WAR locally without Tomcat

```bash
mvn -q -Pprod -DskipTests package
java -jar target/weather.war
```

Opens at `http://localhost:8080/` (no `/weather` prefix unless you set `SERVER_SERVLET_CONTEXT_PATH`). H2 uses `./data/weather-data/` when `catalina.base` is unset.

---

## Deploying alongside an existing Tomcat application

Tomcat runs **multiple WARs on the same instance**. Your existing app and this weather app can coexist without a second Tomcat install.

| Existing app | Weather app |
|--------------|-------------|
| e.g. `relief.war` → `https://host/relief/` | `weather.war` → `https://host/weather/` |
| Keeps its own URLs and data | Separate context; does not replace the other WAR |

### Steps

1. **Build** `target/weather.war` (do not rename the main portal WAR).
2. **Copy** only `weather.war` into the same Tomcat `webapps/` directory where your other WAR already lives.
3. **Avoid context path clash** — do not deploy a second WAR also named `weather.war`, and do not use `SERVER_SERVLET_CONTEXT_PATH` on Tomcat unless you know you need it.
4. **Build with `-Pprod`** and copy `weather.war` — no `SPRING_PROFILES_ACTIVE` on Tomcat. H2 files go under `<tomcat>/weather-data/` by default (separate from other apps).

5. **Restart Tomcat** (or use Tomcat Manager to deploy `weather.war` without touching the running app’s files if your process allows hot deploy).

### Reverse proxy (relief portal)

Route by path on the **same host**:

```nginx
# Existing main app (example)
location / {
    proxy_pass http://127.0.0.1:8080/relief/;   # adjust to your existing context
}

# Weather dashboard
location /weather/ {
    proxy_pass http://127.0.0.1:8080/weather/;
    proxy_set_header X-Forwarded-Proto $scheme;
}
```

Both locations can point to the **same Tomcat port** (e.g. 8080); only the path prefix differs.

### Checks after deploy

| App | Example URL |
|-----|-------------|
| Existing | unchanged (e.g. `https://relief.megrevenuedm.gov.in/`) |
| Weather | `https://relief.megrevenuedm.gov.in/weather/` |

In Tomcat Manager or `webapps/`, you should see **two** deployed contexts (e.g. `/relief` and `/weather`).

### Requirements

- **Tomcat 10.1+** for this WAR (Spring Boot 3). If the existing app is older (Tomcat 9 / `javax.*`), you may need Tomcat 10 for weather while keeping the other app compatible, or run weather on a separate Tomcat port — confirm with your infra team.
- **Memory** — adding a Spring Boot app increases heap use; consider raising Tomcat `CATALINA_OPTS` (e.g. `-Xmx512m` or higher).

---

## Profiles

| Profile | Purpose |
|---------|---------|
| **`local`** (default if unset) | Mock IMD data; faster scheduler crons for dev — **do not use in production** |
| **`prod`** | Live IMD APIs, Thymeleaf cache on, H2 console disabled |

Always set `SPRING_PROFILES_ACTIVE=prod` in production.

---

## Environment variables

| Variable | Default | Description |
|----------|---------|-------------|
| `SPRING_PROFILES_ACTIVE` | `local` | Set to **`prod`** for deployment |
| `SERVER_PORT` | `8080` | HTTP port |
| `SERVER_SERVLET_CONTEXT_PATH` | empty in **`prod`** (default) | Only needed when **not** using `weather.war` on Tomcat (e.g. `java -jar` at `/weather`) |
| `WEATHER_DB_PATH` | `./data/weather-db` | H2 file path **without** `.mv.db` suffix |
| `IMD_CONNECT_TIMEOUT` | `15s` | IMD HTTP connect timeout |
| `IMD_READ_TIMEOUT` | `45s` | IMD HTTP read timeout |
| `IMD_NOWCAST_CRON` | `0 0 0/3 * * *` | Nowcast refresh (every 3 hours) |
| `IMD_DAILY_CRON` | `0 0 9 * * *` | Daily data refresh at 09:00 |
| `IMD_DAILY_RETRY_MS` | `600000` | Retry interval (10 min) if daily data is not “today” |
| `IMD_DAILY_MAX_RETRIES` | `144` | Max daily retries (~24 h at 10 min) |

Example override:

```bash
export IMD_DAILY_CRON="0 0 9 * * *"
export IMD_NOWCAST_CRON="0 0 0/3 * * *"
```

Cron format is **Spring 6-field**: `second minute hour day month weekday`.

---

## Runtime behaviour (production)

1. **Startup** — Loads H2 → in-memory cache, then fetches all IMD endpoints and updates H2 (background thread).
2. **UI / APIs** — Read from cache (populated from H2); schedulers refresh data; browsers do not call IMD directly.
3. **Nowcast** — Scheduler every **3 hours** (`IMD_NOWCAST_CRON`).
4. **Rainfall, warnings, city weather, AWS** — Daily at **09:00**; if response dates are not today, retry every **10 minutes** until fresh, then wait until the next 09:00 run.

Configured IDs are in `src/main/resources/application.yml` (11 district `obj_id`s, 2 city stations, 20 AWS station ids).

---

## Running as a service

**Recommended:** deploy `weather.war` on Tomcat and manage Tomcat with systemd.

### Linux (systemd) — standalone WAR (optional)

If you do not use Tomcat, you can run the executable WAR directly:

```ini
[Unit]
Description=NIC Meghalaya Weather Dashboard
After=network.target

[Service]
Type=simple
User=weather
WorkingDirectory=/opt/nic-weather
Environment=SPRING_PROFILES_ACTIVE=prod
Environment=WEATHER_DB_PATH=/var/lib/nic-weather/weather-db
Environment=SERVER_PORT=8080
ExecStart=/usr/bin/java -jar /opt/nic-weather/weather.war
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
```

Ensure `/var/lib/nic-weather` exists and is owned by `weather`.

---

## Reverse proxy (required when mounted at `/weather`)

The relief portal serves this app at **`https://<host>/weather/`**. Deploy **`weather.war`** on Tomcat so the context path is `/weather`, or proxy to that context.

All HTML, CSS, JS, SVG, and JSON API URLs must be under `/weather/…`. If static assets are requested from `/css/…` or `/api/…` at the domain root, the portal redirects them to login and the dashboard stays on “Loading…”.

Example **nginx** in front of the app:

```nginx
server {
    listen 443 ssl;
    server_name relief.megrevenuedm.gov.in;

    location /weather/ {
        proxy_pass http://127.0.0.1:8080/weather/;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

| Check after deploy | Expected URL |
|--------------------|--------------|
| Dashboard | `https://<host>/weather/` |
| Stylesheet | `https://<host>/weather/css/style.css` |
| Map API | `https://<host>/weather/api/weather/nowcast/map` |
| Health | `https://<host>/weather/actuator/health` |

Override context path if needed: `SERVER_SERVLET_CONTEXT_PATH=/weather` (must match the proxy path prefix).

Terminate TLS at nginx or a load balancer. The app does not require HTTPS for itself when behind a proxy.

**Portal note:** If the main site redirects unauthenticated requests to `http://…/login`, ensure that redirect uses **HTTPS** as well; otherwise browsers block those responses as mixed content on HTTPS pages.

---

## Firewall and networking

| Direction | Port / host | Reason |
|-----------|-------------|--------|
| Inbound | `SERVER_PORT` (8080) | HTTP dashboard and APIs |
| Outbound | `443` → `mausam.imd.gov.in`, `city.imd.gov.in` | IMD data fetch (`prod` only) |

---

## H2 database

- **Location:** `${WEATHER_DB_PATH}.mv.db` (e.g. `./data/weather-db.mv.db`)
- **Console:** Enabled in default `application.yml` for dev; **disabled in `prod`** (`application-prod.yml`).
- **Backup:** Stop the app (or use a filesystem snapshot) and copy the `data/` directory.
- **Restore:** Place files back and start with the same `WEATHER_DB_PATH`.

Do not commit `data/*.mv.db` to source control if it contains environment-specific state.

---

## Upgrades

1. Stop the running instance.
2. Back up `data/`.
3. Deploy the new JAR.
4. Start with the same `WEATHER_DB_PATH` and `prod` profile.

Schema changes are applied automatically (`spring.jpa.hibernate.ddl-auto: update`).

---

## Logs (local profile)

When `SPRING_PROFILES_ACTIVE=local` (default), logs are written to:

| File | Purpose |
|------|---------|
| `./logs/nic-weather-app.log` | Full application log (rolling daily) |
| Console | Same output in the terminal / IDE |

Search the log file for audit lines:

- `IMD_REFRESH_START` / `IMD_REFRESH_END` — which data group was refreshed (nowcast, rainfall, city, etc.)
- `IMD_CALL_START` / `IMD_CALL_OK` / `IMD_CALL_FAIL` — each API and id (`mock=true` = embedded data, `mock=false` = live HTTP)

Override path: `LOG_FILE=C:\logs\weather.log` or `logging.file.name` in `application-local.yml`.

Startup refresh runs **in the background** (non-blocking); the app serves immediately from H2 while APIs run on a worker thread.

---

## Troubleshooting

| Symptom | Likely cause | Action |
|---------|----------------|--------|
| Empty map after deploy | Background refresh still running or first start with empty H2 | Wait 1–2 min; check logs for `Background refresh completed` |
| Stale data | Scheduler disabled or wrong profile | Confirm `SPRING_PROFILES_ACTIVE=prod` and `weather.imd.scheduler.enabled: true` |
| Daily data never updates | IMD not publishing for “today” yet | Check logs for `DailyDataFreshnessChecker`; retries every 10 min until fresh |
| Connection errors | Firewall or IMD outage | Verify outbound HTTPS; increase `IMD_READ_TIMEOUT` |
| Wrong schedule times | Server time zone | Set OS timezone to `Asia/Kolkata` or adjust cron env vars |

Useful loggers (temporary):

```yaml
logging.level.com.example.weathermap.service.imd: DEBUG
```

---

## API reference (this application)

| Method | Path | Description |
|--------|------|-------------|
| GET | `/` | Weather dashboard (Thymeleaf) |
| GET | `/api/weather/nowcast/map` | District nowcast (cached) |
| GET | `/api/weather/rainfall/map` | District rainfall (cached) |
| GET | `/api/weather/district-warning/map` | District warnings (cached) |
| GET | `/api/weather/cities` | City weather panels (cached) |
| GET | `/api/weather/aws-observations` | AWS/ARG observations (cached) |
| GET | `/api/weather/sync-status` | Last refresh times and cron settings |
| GET | `/actuator/health` | Health check |

---

## Local development (not production)

```bash
mvn spring-boot:run
```

Default profile `local` uses mock IMD data and accelerated schedulers. For live APIs locally:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

See the main [README.md](README.md) for a short project overview.
