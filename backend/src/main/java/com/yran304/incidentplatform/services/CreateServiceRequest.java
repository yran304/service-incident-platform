package com.yran304.incidentplatform.services;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateServiceRequest (
    // we need organizationId to tell which organization is this service associated with 
    // but we are not adding it here
    // organizationId comes from the nested API URL, such as /api/organizations/{organizationId}/services 
    // that's how it knows which organization regarding this request

    @NotBlank
    @Size(max = 120)
    String name,

    @NotBlank
    @Size(max = 80)
    @Pattern(
        regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$",
        message = "must contain lowercase letters, numbers, and hyphens only"
    )
    String slug
) {
    
}
