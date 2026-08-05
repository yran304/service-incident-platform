package com.yran304.incidentplatform.services;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TrackedServiceRepository extends JpaRepository<TrackedService, UUID> {
    
    boolean existsByOrganizationIdAndSlug(UUID organizationId, String slug);

    List<TrackedService> findAllByOrganizationId(UUID organizationId);
}
