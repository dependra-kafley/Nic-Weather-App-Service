package com.example.weathermap.repository;

import com.example.weathermap.entity.AwsDataSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AwsDataSnapshotRepository extends JpaRepository<AwsDataSnapshot, Long> {
    Optional<AwsDataSnapshot> findTopByOrderByFetchedAtDesc();
}
