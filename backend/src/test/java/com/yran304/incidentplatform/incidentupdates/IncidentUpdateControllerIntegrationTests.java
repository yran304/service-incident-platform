package com.yran304.incidentplatform.incidentupdates;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.transaction.annotation.Transactional;

import com.yran304.incidentplatform.incidents.Incident;
import com.yran304.incidentplatform.incidents.IncidentRepository;
import com.yran304.incidentplatform.organizations.Organization;
import com.yran304.incidentplatform.organizations.OrganizationRepository;
import com.yran304.incidentplatform.services.TrackedService;
import com.yran304.incidentplatform.services.TrackedServiceRepository;

import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class IncidentUpdateControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private TrackedServiceRepository trackedServiceRepository;

    @Autowired
    private IncidentRepository incidentRepository;

    @Test
    void createsPublishedIncidentUpdate() throws Exception {
        Incident incident = createTestIncident();
        CreateIncidentUpdateRequest request = new CreateIncidentUpdateRequest(
            "We have identified the root cause.",
            true
        );

        mockMvc.perform(
            post("/api/incidents/{incidentId}/updates", incident.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").isNotEmpty())
            .andExpect(jsonPath("$.incidentId").value(incident.getId().toString()))
            .andExpect(jsonPath("$.message").value("We have identified the root cause."))
            .andExpect(jsonPath("$.isPublished").value(true))
            .andExpect(jsonPath("$.publishedAt").isNotEmpty());
    }

    @Test
    void rejectsInvalidIncidentUpdateRequest() throws Exception {
        CreateIncidentUpdateRequest request = new CreateIncidentUpdateRequest("", false); // empty message(invalid)

        mockMvc.perform(
            post("/api/incidents/{incidentId}/updates", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest());
    }

    @Test
    void listsIncidentUpdatesInCreationOrder() throws Exception {
        Incident incident = createTestIncident();
        String firstMessage = "Investigating payment failures " + UUID.randomUUID();
        String secondMessage = "Root cause identified " + UUID.randomUUID();

        createIncidentUpdate(incident.getId(), firstMessage, false)
            .andExpect(status().isCreated());

        createIncidentUpdate(incident.getId(), secondMessage, true)
            .andExpect(status().isCreated());

        mockMvc.perform(
            get("/api/incidents/{incidentId}/updates", incident.getId())
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].message").value(firstMessage))
            .andExpect(jsonPath("$[0].publishedAt").isEmpty())
            .andExpect(jsonPath("$[1].message").value(secondMessage))
            .andExpect(jsonPath("$[1].publishedAt").isNotEmpty());
    }

    @Test
    void returnsNotFoundForNonexistentIncident() throws Exception {
        mockMvc.perform(
            get("/api/incidents/{incidentId}/updates", UUID.randomUUID())
        )
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.title").value("Incident not found"));
    }

    private TrackedService createTestService() {
        Organization organization = organizationRepository.save(
            new Organization(
                UUID.randomUUID(),
                "Test Organization",
                "test-organization-" + UUID.randomUUID(),
                Instant.now()
            )
        );

        return trackedServiceRepository.save(
            new TrackedService(
                UUID.randomUUID(),
                organization.getId(),
                "Test Service",
                "test-service-" + UUID.randomUUID(),
                "OPERATIONAL",
                Instant.now()
            )
        );
    }

    private Incident createTestIncident() {
        TrackedService trackedService = createTestService();
        Instant now = Instant.now();

        return incidentRepository.save(
            new Incident(
                UUID.randomUUID(),
                trackedService.getId(),
                "Test Incident",
                "INVESTIGATING",
                "MAJOR",
                now,
                null,
                now
            )
        );
    }

    private ResultActions createIncidentUpdate(
        UUID incidentId,
        String message,
        boolean isPublished
    ) throws Exception {
        CreateIncidentUpdateRequest request = new CreateIncidentUpdateRequest(
            message,
            isPublished
        );

        return mockMvc.perform(
            post("/api/incidents/{incidentId}/updates", incidentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        );
    }
}
