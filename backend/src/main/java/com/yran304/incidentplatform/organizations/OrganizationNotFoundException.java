package com.yran304.incidentplatform.organizations;

import java.util.UUID;

public class OrganizationNotFoundException extends RuntimeException {

    public OrganizationNotFoundException(UUID organizationId) {
        super("Organization with id '" + organizationId + "' was not found");
    }
}
