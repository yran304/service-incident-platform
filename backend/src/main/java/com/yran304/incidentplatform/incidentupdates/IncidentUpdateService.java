package com.yran304.incidentplatform.incidentupdates;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.yran304.incidentplatform.incidents.IncidentNotFoundException;
import com.yran304.incidentplatform.incidents.IncidentRepository;

@Service
public class IncidentUpdateService {

    private final IncidentRepository incidentRepository;
    private final IncidentUpdateRepository incidentUpdateRepository;

    public IncidentUpdateService(
        IncidentRepository incidentRepository,
        IncidentUpdateRepository incidentUpdateRepository
    ) {
        this.incidentRepository = incidentRepository;
        this.incidentUpdateRepository = incidentUpdateRepository;
    }

    public IncidentUpdateResponse createIncidentUpdate(
        UUID incidentId,
        CreateIncidentUpdateRequest request
    ) {
        if (!incidentRepository.existsById(incidentId)) {
            throw new IncidentNotFoundException(incidentId);
        }

        Instant now = Instant.now();
        IncidentUpdate incidentUpdate = new IncidentUpdate(
            UUID.randomUUID(),
            incidentId,
            request.message(),
            request.isPublished(),
            now,
            request.isPublished() ? now : null
        );

        IncidentUpdate savedIncidentUpdate = incidentUpdateRepository.save(incidentUpdate);

        return IncidentUpdateResponse.from(savedIncidentUpdate);
    }

    public List<IncidentUpdateResponse> getIncidentUpdatesByIncident(UUID incidentId) {
        if (!incidentRepository.existsById(incidentId)) {
            throw new IncidentNotFoundException(incidentId);
        }

        return incidentUpdateRepository
                .findAllByIncidentIdOrderByCreatedAtAsc(incidentId)
                .stream()
                .map(IncidentUpdateResponse::from)
                .toList();
    }
}
