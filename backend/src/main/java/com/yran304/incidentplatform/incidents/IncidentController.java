package com.yran304.incidentplatform.incidents;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
public class IncidentController {

    private final IncidentService incidentService;

    public IncidentController(IncidentService incidentService) {
        this.incidentService = incidentService;
    }

    @PostMapping("/api/services/{serviceId}/incidents")
    @ResponseStatus(HttpStatus.CREATED)
    public IncidentResponse createIncident(
        @PathVariable UUID serviceId,
        @Valid @RequestBody CreateIncidentRequest request
    ) {
        return incidentService.createIncident(serviceId, request);
    }

    @GetMapping("/api/services/{serviceId}/incidents")
    public List<IncidentResponse> getIncidentsByService(
        @PathVariable UUID serviceId
    ) {
        return incidentService.getIncidentsByService(serviceId);
    }

    @PatchMapping("/api/incidents/{incidentId}/status")
    public IncidentResponse updateIncidentStatus(
        @PathVariable UUID incidentId,
        @Valid @RequestBody UpdateIncidentStatusRequest request
    ) {
        return incidentService.updateIncidentStatus(incidentId, request);
    }
    
}
