package com.yran304.incidentplatform.organizations;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
public class OrganizationService { // higher-level business logic. It uses the repository to read/write data and converts between request/entity/response objects.
    // OrganizationService needs the repository because the service itself don't know how to talk to PostgreSQL.
    // final makes it unable to reassign reference once the constructor sets it
    private final OrganizationRepository organizationRepository;

    public OrganizationService(OrganizationRepository organizationRepository) {
        this.organizationRepository = organizationRepository;
    }

    public OrganizationResponse createOrganization(CreateOrganizationRequest request) {
        Organization organization = new Organization(
            UUID.randomUUID(),
            request.name(), // we only need to provide these two when creating a new request.
            request.slug(),
            Instant.now()
        );
        // the step that writes the new row to the table
        try {
            Organization savedOrganization = organizationRepository.saveAndFlush(organization);
            return OrganizationResponse.from(savedOrganization);
        } catch (DataIntegrityViolationException e) {
            throw new OrganizationSlugAlreadyExistsException(request.slug());
        }
    }

    public List<OrganizationResponse> getOrganizations() {
        return organizationRepository.findAll()
                .stream()
                .map(OrganizationResponse::from)
                .toList();
    }
}
