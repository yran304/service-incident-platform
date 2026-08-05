package com.yran304.incidentplatform.services;

import java.time.Instant;
import java.util.UUID;

public record ServiceResponse(
    UUID id,
    UUID organizationId,
    String name,
    String slug,
    String currentStatus,
    Instant createdAt
) {
    public static ServiceResponse from(TrackedService service) {
        return new ServiceResponse(
            service.getId(),
            service.getOrganizationId(),
            service.getName(),
            service.getSlug(),
            service.getCurrentStatus(),
            service.getCreatedAt()
        );
    }
}
