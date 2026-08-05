package com.yran304.incidentplatform.services;

import java.util.UUID;

public class ServiceSlugAlreadyExistsException extends RuntimeException {
    
    public ServiceSlugAlreadyExistsException(UUID organizationId, String slug) {
        super(
            "A service with slug '" + slug
            + "' already exists for organization '" + organizationId + "'"
        );
    }
}
