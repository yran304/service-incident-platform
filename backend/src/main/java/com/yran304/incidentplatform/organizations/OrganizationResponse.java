package com.yran304.incidentplatform.organizations;

import java.time.Instant;
import java.util.UUID;

// Defines the fields exposed by our API, separate from the database entity.
// for now, it looks same as Organization, later they might diverge
public record OrganizationResponse( // the controlled output data sent back to an API client.
    UUID id,
    String name,
    String slug,
    Instant createdAt
) {
    // The from(...) method converts an Organization entity into this API response,
    // so we control exactly what information clients receive.
    public static OrganizationResponse from(Organization organization) { // not rellying on a OrganizationResponse object
        return new OrganizationResponse(
                organization.getId(),
                organization.getName(),
                organization.getSlug(),
                organization.getCreatedAt()
        );
    }
}
