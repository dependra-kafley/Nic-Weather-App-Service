package com.example.weathermap.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "RAINFALL_SNAPSHOT")
public class RainfallSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fetched_at", nullable = false)
    private Instant fetchedAt;

    @Lob
    @Column(name = "payload", nullable = false, columnDefinition = "CLOB")
    private String payload;

    public RainfallSnapshot() {}

    public RainfallSnapshot(Instant fetchedAt, String payload) {
        this.fetchedAt = fetchedAt;
        this.payload = payload;
    }

    public Long getId() { return id; }
    public Instant getFetchedAt() { return fetchedAt; }
    public String getPayload() { return payload; }
}
