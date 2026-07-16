package com.yran304.incidentplatform.organizations;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// record instead of class. record keyword gives us a small immutable data class automatically—no constructor or getters needed.
public record CreateOrganizationRequest( // the allowed input data from an API client when creating an organization.

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