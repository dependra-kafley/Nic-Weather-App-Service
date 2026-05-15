# NIC Weather App Service

Spring Boot dashboard for Meghalaya district weather using public IMD APIs.

## Features

- Interactive district map (nowcast, rainfall, district-wise warning layers)
- IMD city weather accordion (configurable station ids)
- No database required — data is fetched from IMD on each request

## Stack

- Java 17, Spring Boot 3.3
- Thymeleaf + Bootstrap UI
- Spring RestClient with configurable timeouts
- Spring Boot Actuator (`/actuator/health`)

## Run locally (mock data — default)

By default the app uses the **`local` profile** with **mock IMD data** (no internet required). Good for testing the map, rainfall, warnings, and city accordion.

```bash
mvn spring-boot:run
```

Open http://localhost:8080 — you should see coloured districts and Tura/Shillong city panels.

On startup, logs show: `IMD MOCK MODE — using embedded test data`.

### Profiles

| Profile | Use |
|---------|-----|
| **`local`** (default) | All IMD endpoints mocked |
| **`mock`** | Same as local |
| **`prod`** | Live IMD APIs (`mock: false`) |

Live IMD + schedulers:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=prod
# or
set SPRING_PROFILES_ACTIVE=prod
mvn spring-boot:run
```

## Production

```bash
mvn -q -DskipTests package
java -jar target/nic-weather-app-service-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

Environment variables (optional):

| Variable | Description |
|----------|-------------|
| `SERVER_PORT` | HTTP port (default 8080) |
| `IMD_CONNECT_TIMEOUT` | e.g. `15s` |
| `IMD_READ_TIMEOUT` | e.g. `45s` |

## API endpoints

## External IMD APIs (called by this service)

| Data | Base URL | Query | Schedule |
|------|----------|-------|----------|
| **District nowcast** | `https://mausam.imd.gov.in/api/nowcast_district_api.php` | `?id={obj_id}` | **Every 3 hours** |
| **District rainfall** | `https://mausam.imd.gov.in/api/districtwise_rainfall_api.php` | `?id={obj_id}` | **Daily** (01:00) |
| **District warning** | `https://mausam.imd.gov.in/api/warnings_district_api.php` | `?id={obj_id}` | **Daily** (01:00) |
| **City weather** | `https://city.imd.gov.in/api/cityweather.php` | `?id={station_id}` | **Daily** (01:00) |

**District `obj_id` values** (Meghalaya): `1`–`7`, `575`, `672`, `673`, `674`  
**City station ids**: `42516` (Shillong), `99489` (Tura)

Schedulers: `weather.imd.scheduler` in `application.yml`. Override with `IMD_NOWCAST_CRON` / `IMD_DAILY_CRON`.

## API endpoints (this app)

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/weather/nowcast/map` | Cached district nowcast for map |
| GET | `/api/weather/rainfall/map` | Cached district rainfall for map |
| GET | `/api/weather/district-warning/map` | Cached district warnings for map |
| GET | `/api/weather/cities` | Cached city weather panels |
| GET | `/api/weather/sync-status` | Last refresh times and scheduler cron |
| GET | `/actuator/health` | Health check |
