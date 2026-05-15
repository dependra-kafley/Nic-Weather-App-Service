package com.example.weathermap.repository;

import com.example.weathermap.entity.RainfallSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RainfallSnapshotRepository extends JpaRepository<RainfallSnapshot, Long> {
    Optional<RainfallSnapshot> findTopByOrderByFetchedAtDesc();
}
