package com.yran304.incidentplatform.incidentupdates;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
public class IncidentUpdateController {

    private final IncidentUpdateService incidentUpdateService;

    public IncidentUpdateController(IncidentUpdateService incidentUpdateService) {
        this.incidentUpdateService = incidentUpdateService;
    }

    @PostMapping("/api/incidents/{incidentId}/updates")
    @ResponseStatus(HttpStatus.CREATED)
    public IncidentUpdateResponse createIncidentUpdate(
        @PathVariable UUID incidentId,
        @Valid @RequestBody CreateIncidentUpdateRequest request
    ) {
        return incidentUpdateService.createIncidentUpdate(incidentId, request);
    }

    @GetMapping("/api/incidents/{incidentId}/updates")
    public List<IncidentUpdateResponse> getIncidentUpdatesByIncident(
        @PathVariable UUID incidentId
    ) {
        return incidentUpdateService.getIncidentUpdatesByIncident(incidentId);
    }
}
