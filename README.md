# NIC Weather App Service

Spring Boot dashboard for **Meghalaya** district weather using public [IMD](https://mausam.imd.gov.in/) APIs. Data is fetched on a schedule, stored in **H2**, and served to the UI and REST APIs from an in-memory cache.

## Features

- Interactive district map: nowcast, rainfall, district warnings
- City weather panels (Shillong, Tura)
- AWS/ARG rainfall observations (grouped tables)
- Scheduled refresh: nowcast every 3 hours; other datasets daily at 9 AM with 10-minute retries until fresh

## Stack

- Java 17, Spring Boot 3.3
- Thymeleaf + Bootstrap UI
- H2 (file-based) + JPA snapshots
- Spring scheduling + Actuator (`/actuator/health`)

## Quick start (development)

Default profile is **`local`** (mock IMD data, no internet required):

```bash
mvn spring-boot:run
```

Or run `WeatherMapApplication` from your IDE (Maven **local** profile must be active — reimport Maven after clone).

Open http://localhost:8080

**Logs (local profile):** `./logs/nic-weather-app.log` — includes every IMD API call (`IMD_CALL_*`) and refresh summary (`IMD_REFRESH_*`). Daily archives: `nic-weather-app.log.yyyy-MM-dd.gz`.

Live IMD APIs:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

## Deployment

**See [DEPLOYMENT.md](DEPLOYMENT.md)** for production WAR build (`mvn -Pprod package` → `target/weather.war`), Tomcat deploy, H2 backup, reverse proxy, and troubleshooting.

Minimal production run:

```bash
mvn -q -DskipTests package
set SPRING_PROFILES_ACTIVE=prod
java -jar target/nic-weather-app-service-0.0.1-SNAPSHOT.jar
```

## Profiles

| Profile | Use |
|---------|-----|
| `local` (default) | Mock IMD data; faster schedulers for dev |
| `prod` | Live IMD APIs; H2 console disabled |

## API endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/weather/nowcast/map` | District nowcast for map |
| GET | `/api/weather/rainfall/map` | District rainfall for map |
| GET | `/api/weather/district-warning/map` | District warnings for map |
| GET | `/api/weather/cities` | City weather panels |
| GET | `/api/weather/aws-observations` | AWS/ARG grouped observations |
| GET | `/api/weather/sync-status` | Last refresh times and scheduler cron |
| GET | `/actuator/health` | Health check |

## External IMD sources

| Data | URL pattern | Schedule (prod) |
|------|-------------|-----------------|
| District nowcast | `nowcast_district_api.php?id={obj_id}` | Every 3 hours |
| District rainfall | `districtwise_rainfall_api.php?id={obj_id}` | Daily 09:00 |
| District warning | `warnings_district_api.php?id={obj_id}` | Daily 09:00 |
| City weather | `cityweather.php?id={station_id}` | Daily 09:00 |
| AWS data | `aws_data_api.php?id={station_id}` | Daily 09:00 |

District `obj_id` values and station ids are configured in `src/main/resources/application.yml`.
