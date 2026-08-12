package com.yran304.incidentplatform.incidentupdates;

import java.time.Instant;
import java.util.UUID;

public record IncidentUpdateResponse(
    UUID id,
    UUID incidentId,
    String message,
    boolean isPublished,
    Instant createdAt,
    Instant publishedAt
) {
    public static IncidentUpdateResponse from(IncidentUpdate incidentUpdate) {
        return new IncidentUpdateResponse(
            incidentUpdate.getId(),
            incidentUpdate.getIncidentId(),
            incidentUpdate.getMessage(),
            incidentUpdate.isPublished(),
            incidentUpdate.getCreatedAt(),
            incidentUpdate.getPublishedAt()
        );
    }
}
