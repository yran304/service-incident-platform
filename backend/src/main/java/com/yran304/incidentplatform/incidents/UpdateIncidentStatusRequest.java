package com.yran304.incidentplatform.incidents;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UpdateIncidentStatusRequest(
    @NotBlank
    @Pattern(
        regexp = "^(INVESTIGATING|IDENTIFIED|MONITORING|RESOLVED)$",
        message = "must be INVESTIGATING, IDENTIFIED, MONITORING, or RESOLVED"
    )
    String status
) {}
