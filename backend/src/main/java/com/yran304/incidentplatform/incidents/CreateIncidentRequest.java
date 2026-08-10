package com.yran304.incidentplatform.incidents;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateIncidentRequest(
    @NotBlank
    @Size(max = 200)
    String title,

    @NotBlank
    @Pattern(
        regexp = "^(MINOR|MAJOR|CRITICAL)$",
        message = "must be MINOR, MAJOR, or CRITICAL"
    )
    String impact
) {}
