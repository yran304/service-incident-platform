package com.yran304.incidentplatform.incidents;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.yran304.incidentplatform.services.TrackedServiceNotFoundException;
import com.yran304.incidentplatform.services.TrackedServiceRepository;

@Service
public class IncidentService {

    private final TrackedServiceRepository trackedServiceRepository;
    private final IncidentRepository incidentRepository;

    public IncidentService(
        TrackedServiceRepository trackedServiceRepository,
        IncidentRepository incidentRepository
    ) {
        this.trackedServiceRepository = trackedServiceRepository;
        this.incidentRepository = incidentRepository;
    }

    public IncidentResponse createIncident(
        UUID serviceId,
        CreateIncidentRequest request
    ) {
        if (!trackedServiceRepository.existsById(serviceId)) {
            throw new TrackedServiceNotFoundException(serviceId);
        }

        Instant now = Instant.now();
        Incident incident = new Incident(
            UUID.randomUUID(),
            serviceId,
            request.title(),
            "INVESTIGATING",
            request.impact(),
            now,
            null,
            now
        );

        Incident savedIncident = incidentRepository.save(incident);

        return IncidentResponse.from(savedIncident);
    }

    public List<IncidentResponse> getIncidentsByService(UUID serviceId) {
        if (!trackedServiceRepository.existsById(serviceId)) {
            throw new TrackedServiceNotFoundException(serviceId);
        }

        return incidentRepository.findAllByServiceId(serviceId)
                .stream()
                .map(IncidentResponse::from)
                .toList();
    }

    public IncidentResponse updateIncidentStatus(
        UUID incidentId,
        UpdateIncidentStatusRequest request
    ) {
        Incident incident = incidentRepository.findById(incidentId)
            .orElseThrow(() -> new IncidentNotFoundException(incidentId));

        if (!isValidStatusTransition(
            incident.getStatus(), 
            request.status()
        )) {
            throw new InvalidIncidentStatusTransitionException(
                incident.getStatus(), 
                request.status()
            );
        }

        Instant resolvedAt = request.status().equals("RESOLVED") 
            ? Instant.now() 
            : null;
        
        incident.updateStatus(request.status(), resolvedAt);

        Incident savedIncident = incidentRepository.save(incident);

        return IncidentResponse.from(savedIncident);
        
    }

    private boolean isValidStatusTransition(
        String currentStatus,
        String requestedStatus
    ) {
        return switch (currentStatus) {
            case "INVESTIGATING" -> requestedStatus.equals("IDENTIFIED");
            case "IDENTIFIED" -> requestedStatus.equals("MONITORING");
            case "MONITORING" -> requestedStatus.equals("RESOLVED");
            case "RESOLVED" -> false;
            default -> false;
        };
    }
}
