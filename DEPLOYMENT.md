# Deployment Guide — NIC Weather App Service

This document describes how to build, configure, and run **Nic-Weather-App-Service** in production. The app is a Spring Boot 3.3 service (Java 17) that caches IMD weather data in an on-disk **H2** database and serves a Thymeleaf dashboard plus JSON APIs.

---

## Prerequisites

| Requirement | Notes |
|-------------|--------|
| **JDK 17** | Required at build and runtime |
| **Maven 3.9+** | To build the JAR (or use a pre-built artifact from CI) |
| **Outbound HTTPS** | Hosts must reach `mausam.imd.gov.in` and `city.imd.gov.in` when using the `prod` profile |
| **Disk** | Writable directory for H2 files (default `./data/`) |
| **Time zone** | Server clock should be correct; schedulers use the JVM default time zone (typically set the OS to `Asia/Kolkata` for 9 AM daily jobs) |

---

## Quick production deploy

### 1. Build

```bash
cd Nic-Weather-App-Service
mvn -q -DskipTests package
```

Artifact: `target/nic-weather-app-service-0.0.1-SNAPSHOT.jar`

### 2. Prepare data directory

```bash
mkdir -p data
```

H2 creates `weather-db.mv.db` (and related files) under this path. **Back up `data/`** before upgrades or server moves.

### 3. Run with the `prod` profile

**Linux / macOS:**

```bash
export SPRING_PROFILES_ACTIVE=prod
export WEATHER_DB_PATH=./data/weather-db
export SERVER_PORT=8080

java -jar target/nic-weather-app-service-0.0.1-SNAPSHOT.jar
```

**Windows (PowerShell):**

```powershell
$env:SPRING_PROFILES_ACTIVE = "prod"
$env:WEATHER_DB_PATH = ".\data\weather-db"
$env:SERVER_PORT = "8080"

java -jar target\nic-weather-app-service-0.0.1-SNAPSHOT.jar
```

### 4. Verify

| Check | URL / action |
|-------|----------------|
| Dashboard | `http://<host>:8080/` |
| Health | `http://<host>:8080/actuator/health` |
| Sync status | `http://<host>:8080/api/weather/sync-status` |

On first start the app loads any existing H2 snapshots, then refreshes all IMD APIs in the background. Allow 1–2 minutes before expecting a full map.

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

### Linux (systemd)

Create `/etc/systemd/system/nic-weather.service`:

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
ExecStart=/usr/bin/java -jar /opt/nic-weather/nic-weather-app-service-0.0.1-SNAPSHOT.jar
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
```

```bash
sudo systemctl daemon-reload
sudo systemctl enable nic-weather
sudo systemctl start nic-weather
sudo systemctl status nic-weather
```

Ensure `/var/lib/nic-weather` exists and is owned by `weather`.

### Windows service

Use [WinSW](https://github.com/winsw/winsw) or NSSM to wrap:

```text
java.exe -jar C:\apps\nic-weather\nic-weather-app-service-0.0.1-SNAPSHOT.jar
```

Set environment variables `SPRING_PROFILES_ACTIVE=prod` and `WEATHER_DB_PATH` in the service configuration.

---

## Reverse proxy (optional)

Example **nginx** in front of the app:

```nginx
server {
    listen 80;
    server_name weather.example.gov.in;

    location / {
        proxy_pass http://127.0.0.1:8080;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

Terminate TLS at nginx or a load balancer. The app does not require HTTPS for itself when behind a proxy.

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
