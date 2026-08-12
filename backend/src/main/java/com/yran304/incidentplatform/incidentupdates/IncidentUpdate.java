package com.yran304.incidentplatform.incidentupdates;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "incident_updates")
public class IncidentUpdate {

    @Id
    private UUID id;

    @Column(name = "incident_id", nullable = false)
    private UUID incidentId;

    @Column(nullable = false)
    private String message;

    @Column(name = "is_published", nullable = false)
    private boolean isPublished;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "published_at")
    private Instant publishedAt;

    protected IncidentUpdate() {}

    public IncidentUpdate(
        UUID id,
        UUID incidentId,
        String message,
        boolean isPublished,
        Instant createdAt,
        Instant publishedAt
    ) {
        this.id = id;
        this.incidentId = incidentId;
        this.message = message;
        this.isPublished = isPublished;
        this.createdAt = createdAt;
        this.publishedAt = publishedAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getIncidentId() {
        return incidentId;
    }

    public String getMessage() {
        return message;
    }

    public boolean isPublished() {
        return isPublished;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }
}
