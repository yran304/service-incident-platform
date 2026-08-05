package com.yran304.incidentplatform.services;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "services")
public class TrackedService {
    
    @Id
    private UUID id;

    @Column(name="organization_id", nullable=false)
    private UUID organizationId;

    @Column(nullable=false, length=120)
    private String name;

    @Column(nullable=false, length=80)
    private String slug;

    @Column(name="current_status", nullable=false, length=30)
    private String currentStatus;

    @Column(name="created_at", nullable=false)
    private Instant createdAt;

    protected TrackedService() {}

    public TrackedService(
        UUID id,
        UUID organizationId,
        String name,
        String slug,
        String currentStatus,
        Instant createdAt
    ) {
        this.id = id;
        this.organizationId = organizationId;
        this.name = name;
        this.slug = slug;
        this.currentStatus = currentStatus;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getOrganizationId() {
        return organizationId;
    }

    public String getName() {
        return name;
    }

    public String getSlug() {
        return slug;
    }

    public String getCurrentStatus() {
        return currentStatus;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
    
}
