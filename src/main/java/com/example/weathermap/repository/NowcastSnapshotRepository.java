package com.example.weathermap.repository;

import com.example.weathermap.entity.NowcastSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface NowcastSnapshotRepository extends JpaRepository<NowcastSnapshot, Long> {
    Optional<NowcastSnapshot> findTopByOrderByFetchedAtDesc();
}
