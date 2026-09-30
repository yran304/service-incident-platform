package com.yran304.incidentplatform.services;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.yran304.incidentplatform.organizations.OrganizationNotFoundException;
import com.yran304.incidentplatform.organizations.OrganizationRepository;

@Service
public class TrackedServiceService {
    
    private final OrganizationRepository organizationRepository;
    private final TrackedServiceRepository trackedServiceRepository;

    public TrackedServiceService(
        OrganizationRepository organizationRepository,
        TrackedServiceRepository trackedServiceRepository
    ) {
        this.organizationRepository = organizationRepository;
        this.trackedServiceRepository = trackedServiceRepository;
    }

    public ServiceResponse createService(
        UUID organizationId,
        CreateServiceRequest request
    ) {
        if (!organizationRepository.existsById(organizationId)) {
            throw new OrganizationNotFoundException(organizationId);
        }

        TrackedService service = new TrackedService(
            UUID.randomUUID(),
            organizationId,
            request.name(),
            request.slug(),
            "OPERATIONAL",
            Instant.now()
        );

        try {
            TrackedService savedTrackedService = trackedServiceRepository.saveAndFlush(service);
            return ServiceResponse.from(savedTrackedService);
        } catch (DataIntegrityViolationException e) {
            throw new ServiceSlugAlreadyExistsException(
                organizationId,
                request.slug()
            );
        }
    }

    public List<ServiceResponse> getServicesByOrganization(UUID organizationId) {
        if (!organizationRepository.existsById(organizationId)) {
            throw new OrganizationNotFoundException(organizationId);
        }
        
        return trackedServiceRepository.findAllByOrganizationId(organizationId)
                .stream()
                .map(ServiceResponse::from)
                .toList();
    }
}
