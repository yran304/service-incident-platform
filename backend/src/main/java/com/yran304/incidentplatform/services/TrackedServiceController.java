package com.yran304.incidentplatform.services;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/organizations/{organizationId}/services")
public class TrackedServiceController {
    
    private final TrackedServiceService trackedServiceService;

    public TrackedServiceController(TrackedServiceService trackedServiceService) {
        this.trackedServiceService = trackedServiceService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ServiceResponse createService(
        @PathVariable UUID organizationId,
        @Valid @RequestBody CreateServiceRequest request
    ) {
        return trackedServiceService.createService(organizationId, request);
    }

    @GetMapping
    public List<ServiceResponse> getServicesByOrganization(
        @PathVariable UUID organizationId
    ) {
        return trackedServiceService.getServicesByOrganization(organizationId);
    }
}
