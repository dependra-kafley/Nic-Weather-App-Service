package com.example.weathermap.service.imd;

import com.example.weathermap.dto.CityWeatherPanelItemDto;
import com.example.weathermap.dto.DistrictWarningMapPointResponse;
import com.example.weathermap.dto.NowcastMapPointResponse;
import com.example.weathermap.dto.RainfallMapPointResponse;
import com.example.weathermap.entity.CityWeatherSnapshot;
import com.example.weathermap.entity.NowcastSnapshot;
import com.example.weathermap.entity.RainfallSnapshot;
import com.example.weathermap.entity.WarningSnapshot;
import com.example.weathermap.repository.CityWeatherSnapshotRepository;
import com.example.weathermap.repository.NowcastSnapshotRepository;
import com.example.weathermap.repository.RainfallSnapshotRepository;
import com.example.weathermap.repository.WarningSnapshotRepository;
import com.example.weathermap.entity.AwsDataSnapshot;
import com.example.weathermap.repository.AwsDataSnapshotRepository;
import com.example.weathermap.dto.ImdAwsDataGroupedResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persists each IMD data type as a JSON blob snapshot in H2, and
 * restores the latest snapshot into the in-memory cache on startup.
 *
 * <p>Each save replaces all previous rows for that type (only the latest
 * snapshot is kept, to avoid unbounded growth).</p>
 */
@Service
public class WeatherSnapshotStore {

    private static final Logger log = LoggerFactory.getLogger(WeatherSnapshotStore.class);

    private final NowcastSnapshotRepository nowcastRepo;
    private final RainfallSnapshotRepository rainfallRepo;
    private final WarningSnapshotRepository warningRepo;
    private final CityWeatherSnapshotRepository cityRepo;
    private final AwsDataSnapshotRepository awsRepo;
    private final ObjectMapper objectMapper;

    public WeatherSnapshotStore(
            NowcastSnapshotRepository nowcastRepo,
            RainfallSnapshotRepository rainfallRepo,
            WarningSnapshotRepository warningRepo,
            CityWeatherSnapshotRepository cityRepo,
            AwsDataSnapshotRepository awsRepo,
            ObjectMapper objectMapper
    ) {
        this.nowcastRepo = nowcastRepo;
        this.rainfallRepo = rainfallRepo;
        this.warningRepo = warningRepo;
        this.cityRepo = cityRepo;
        this.awsRepo = awsRepo;
        this.objectMapper = objectMapper;
    }

    // ──────────────────────────────── SAVE ────────────────────────────────

    @Transactional
    public void saveNowcast(List<NowcastMapPointResponse> data) {
        try {
            String json = objectMapper.writeValueAsString(data);
            nowcastRepo.deleteAll();
            nowcastRepo.save(new NowcastSnapshot(Instant.now(), json));
            log.info("Saved nowcast snapshot to H2 ({} districts)", data.size());
        } catch (Exception e) {
            log.error("Failed to save nowcast snapshot to H2: {}", e.getMessage());
        }
    }

    @Transactional
    public void saveRainfall(List<RainfallMapPointResponse> data) {
        try {
            String json = objectMapper.writeValueAsString(data);
            rainfallRepo.deleteAll();
            rainfallRepo.save(new RainfallSnapshot(Instant.now(), json));
            log.info("Saved rainfall snapshot to H2 ({} districts)", data.size());
        } catch (Exception e) {
            log.error("Failed to save rainfall snapshot to H2: {}", e.getMessage());
        }
    }

    @Transactional
    public void saveWarning(List<DistrictWarningMapPointResponse> data) {
        try {
            String json = objectMapper.writeValueAsString(data);
            warningRepo.deleteAll();
            warningRepo.save(new WarningSnapshot(Instant.now(), json));
            log.info("Saved warning snapshot to H2 ({} districts)", data.size());
        } catch (Exception e) {
            log.error("Failed to save warning snapshot to H2: {}", e.getMessage());
        }
    }

    @Transactional
    public void saveCityWeather(List<CityWeatherPanelItemDto> data) {
        try {
            String json = objectMapper.writeValueAsString(data);
            cityRepo.deleteAll();
            cityRepo.save(new CityWeatherSnapshot(Instant.now(), json));
            log.info("Saved city weather snapshot to H2 ({} stations)", data.size());
        } catch (Exception e) {
            log.error("Failed to save city weather snapshot to H2: {}", e.getMessage());
        }
    }

    @Transactional
    public void saveAwsData(ImdAwsDataGroupedResponse data) {
        try {
            String json = objectMapper.writeValueAsString(data);
            awsRepo.deleteAll();
            awsRepo.save(new AwsDataSnapshot(Instant.now(), json));
            log.info("Saved AWS grouped snapshot to H2");
        } catch (Exception e) {
            log.error("Failed to save AWS data snapshot to H2: {}", e.getMessage());
        }
    }

    // ──────────────────────────────── LOAD ────────────────────────────────

    public void loadAllIntoCache(ImdWeatherDataCache cache) {
        loadNowcastIntoCache(cache);
        loadDailyIntoCache(cache);
    }

    private void loadNowcastIntoCache(ImdWeatherDataCache cache) {
        nowcastRepo.findTopByOrderByFetchedAtDesc().ifPresentOrElse(snapshot -> {
            try {
                List<NowcastMapPointResponse> data = objectMapper.readValue(
                        snapshot.getPayload(),
                        new TypeReference<List<NowcastMapPointResponse>>() {});
                cache.updateNowcast(data);
                log.info("Loaded {} nowcast records from H2 snapshot (fetched {})",
                        data.size(), snapshot.getFetchedAt());
            } catch (Exception e) {
                log.error("Failed to load nowcast snapshot from H2: {}", e.getMessage());
            }
        }, () -> log.info("No nowcast snapshot found in H2 – will fetch from IMD API."));
    }

    private void loadDailyIntoCache(ImdWeatherDataCache cache) {
        // Rainfall
        List<RainfallMapPointResponse> rainfall = List.of();
        var rainfallSnap = rainfallRepo.findTopByOrderByFetchedAtDesc();
        if (rainfallSnap.isPresent()) {
            try {
                rainfall = objectMapper.readValue(rainfallSnap.get().getPayload(),
                        new TypeReference<List<RainfallMapPointResponse>>() {});
                log.info("Loaded {} rainfall records from H2 snapshot", rainfall.size());
            } catch (Exception e) {
                log.error("Failed to load rainfall snapshot from H2: {}", e.getMessage());
            }
        } else {
            log.info("No rainfall snapshot found in H2.");
        }

        // Warnings
        List<DistrictWarningMapPointResponse> warnings = List.of();
        var warningSnap = warningRepo.findTopByOrderByFetchedAtDesc();
        if (warningSnap.isPresent()) {
            try {
                warnings = objectMapper.readValue(warningSnap.get().getPayload(),
                        new TypeReference<List<DistrictWarningMapPointResponse>>() {});
                log.info("Loaded {} warning records from H2 snapshot", warnings.size());
            } catch (Exception e) {
                log.error("Failed to load warning snapshot from H2: {}", e.getMessage());
            }
        } else {
            log.info("No warning snapshot found in H2.");
        }

        // City Weather
        List<CityWeatherPanelItemDto> cities = List.of();
        var citySnap = cityRepo.findTopByOrderByFetchedAtDesc();
        if (citySnap.isPresent()) {
            try {
                cities = objectMapper.readValue(citySnap.get().getPayload(),
                        new TypeReference<List<CityWeatherPanelItemDto>>() {});
                log.info("Loaded {} city weather records from H2 snapshot", cities.size());
            } catch (Exception e) {
                log.error("Failed to load city weather snapshot from H2: {}", e.getMessage());
            }
        } else {
            log.info("No city weather snapshot found in H2.");
        }

        // AWS Data
        ImdAwsDataGroupedResponse awsData = new ImdAwsDataGroupedResponse(List.of(), List.of(), List.of());
        var awsSnap = awsRepo.findTopByOrderByFetchedAtDesc();
        if (awsSnap.isPresent()) {
            try {
                awsData = objectMapper.readValue(awsSnap.get().getPayload(), ImdAwsDataGroupedResponse.class);
                log.info("Loaded AWS grouped data from H2 snapshot");
            } catch (Exception e) {
                log.error("Failed to load AWS data snapshot from H2: {}", e.getMessage());
            }
        } else {
            log.info("No AWS data snapshot found in H2.");
        }

        if (!rainfall.isEmpty() || !warnings.isEmpty() || !cities.isEmpty() || !awsData.aws().isEmpty()) {
            cache.updateDaily(rainfall, warnings, cities, awsData);
        }
    }
}
