package com.example.weathermap.repository;

import com.example.weathermap.entity.CityWeatherSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CityWeatherSnapshotRepository extends JpaRepository<CityWeatherSnapshot, Long> {
    Optional<CityWeatherSnapshot> findTopByOrderByFetchedAtDesc();
}
