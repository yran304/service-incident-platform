package com.yran304.incidentplatform.incidentupdates;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateIncidentUpdateRequest(
    @NotBlank
    @Size(max = 2_000)
    String message,

    boolean isPublished
) {}
