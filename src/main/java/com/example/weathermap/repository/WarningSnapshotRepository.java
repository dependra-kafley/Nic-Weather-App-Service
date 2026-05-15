package com.example.weathermap.repository;

import com.example.weathermap.entity.WarningSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface WarningSnapshotRepository extends JpaRepository<WarningSnapshot, Long> {
    Optional<WarningSnapshot> findTopByOrderByFetchedAtDesc();
}
