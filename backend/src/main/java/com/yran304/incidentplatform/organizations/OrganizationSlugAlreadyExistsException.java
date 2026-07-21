package com.yran304.incidentplatform.organizations;

public class OrganizationSlugAlreadyExistsException extends RuntimeException {
    
    public OrganizationSlugAlreadyExistsException(String slug) {
        super("An organization with slug '" + slug + "' already exists");
    }
}
