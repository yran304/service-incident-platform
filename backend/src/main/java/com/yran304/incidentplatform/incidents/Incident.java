package com.yran304.incidentplatform.incidents;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "incidents")
public class Incident {

    @Id
    private UUID id;

    @Column(name = "service_id", nullable = false)
    private UUID serviceId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 30)
    private String status;

    @Column(nullable = false, length = 30)
    private String impact;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Incident() {}

    public Incident(
        UUID id,
        UUID serviceId,
        String title,
        String status,
        String impact,
        Instant startedAt,
        Instant resolvedAt,
        Instant createdAt
    ) {
        this.id = id;
        this.serviceId = serviceId;
        this.title = title;
        this.status = status;
        this.impact = impact;
        this.startedAt = startedAt;
        this.resolvedAt = resolvedAt;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getServiceId() {
        return serviceId;
    }

    public String getTitle() {
        return title;
    }

    public String getStatus() {
        return status;
    }

    public String getImpact() {
        return impact;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getResolvedAt() {
        return resolvedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void updateStatus(String status, Instant resolvedAt) {
        this.status = status;
        this.resolvedAt = resolvedAt;
    }
}
