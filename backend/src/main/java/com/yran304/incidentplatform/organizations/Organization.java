package com.yran304.incidentplatform.organizations;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity // defines entity, the java object and its mapping to the database table.
@Table(name = "organizations")
public class Organization { // the Java entity shape for one row in the organizations table.
    
    @Id
    private UUID id;

    @Column(nullable=false, length=120)
    private String name;

    @Column(nullable=false, unique=true, length=80)
    private String slug;

    @Column(name="created_at", nullable=false)
    private Instant createdAt;

    protected  Organization() { // JPA dictates "Every Entity must have a no-argument constructor"
    }

    public Organization(UUID id, String name, String slug, Instant createdAt) {
        this.id = id;
        this.name = name;
        this.slug = slug;
        this.createdAt = createdAt;
    }

    public UUID getId() { // these getters are for the data that's already retrieved by repository operations.
        return id;
    }

    public String getName() { // they are not accessing the database
        return name;
    }

    public String getSlug() {
        return slug;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
