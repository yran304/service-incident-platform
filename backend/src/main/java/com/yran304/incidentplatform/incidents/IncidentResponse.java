package com.yran304.incidentplatform.incidents;

import java.time.Instant;
import java.util.UUID;

public record IncidentResponse(
    UUID id,
    UUID serviceId,
    String title,
    String status,
    String impact,
    Instant startedAt,
    Instant resolvedAt,
    Instant createdAt
) {
    public static IncidentResponse from(Incident incident) {
        return new IncidentResponse(
            incident.getId(),
            incident.getServiceId(),
            incident.getTitle(),
            incident.getStatus(),
            incident.getImpact(),
            incident.getStartedAt(),
            incident.getResolvedAt(),
            incident.getCreatedAt()
        );
    }
}
