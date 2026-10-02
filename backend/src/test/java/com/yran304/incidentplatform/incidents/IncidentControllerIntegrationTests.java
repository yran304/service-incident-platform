package com.yran304.incidentplatform.incidents;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.transaction.annotation.Transactional;

import com.yran304.incidentplatform.organizations.Organization;
import com.yran304.incidentplatform.organizations.OrganizationRepository;
import com.yran304.incidentplatform.services.TrackedService;
import com.yran304.incidentplatform.services.TrackedServiceRepository;

import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class IncidentControllerIntegrationTests {

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

    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @Test
    void createsIncident() throws Exception {
        TrackedService trackedService = createTestService();
        CreateIncidentRequest request = new CreateIncidentRequest(
            "Payment requests are failing",
            "CRITICAL"
        );

        mockMvc.perform(
            post("/api/services/{serviceId}/incidents", trackedService.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").isNotEmpty())
            .andExpect(jsonPath("$.serviceId").value(trackedService.getId().toString()))
            .andExpect(jsonPath("$.title").value("Payment requests are failing"))
            .andExpect(jsonPath("$.impact").value("CRITICAL"))
            .andExpect(jsonPath("$.status").value("INVESTIGATING"))
            .andExpect(jsonPath("$.resolvedAt").isEmpty());
    }

    @Test
    void rejectsInvalidIncidentRequest() throws Exception {
        CreateIncidentRequest request = new CreateIncidentRequest(
            "",
            "SEVERE"
        );

        mockMvc.perform(
            post("/api/services/{serviceId}/incidents", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest());
    }

    @Test
    void listsIncidentsForService() throws Exception {
        TrackedService trackedService = createTestService();
        String title = "Listed incident " + UUID.randomUUID();
        CreateIncidentRequest request = new CreateIncidentRequest(title, "MAJOR");

        mockMvc.perform(
            post("/api/services/{serviceId}/incidents", trackedService.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated());

        mockMvc.perform(
            get("/api/services/{serviceId}/incidents", trackedService.getId())
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[?(@.title == '%s')]", title).exists());
    }

    @Test
    void returnsNotFoundForNonexistentService() throws Exception {
        mockMvc.perform(
            get("/api/services/{serviceId}/incidents", UUID.randomUUID())
        )
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.title").value("Service not found"));
    }

    @Test
    void updatesIncidentThroughLifecycleAndSetsResolvedAt() throws Exception {
        Incident incident = createTestIncident();

        updateIncidentStatus(incident.getId(), "IDENTIFIED")
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("IDENTIFIED"))
            .andExpect(jsonPath("$.resolvedAt").isEmpty());

        updateIncidentStatus(incident.getId(), "MONITORING")
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("MONITORING"));

        updateIncidentStatus(incident.getId(), "RESOLVED")
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("RESOLVED"))
            .andExpect(jsonPath("$.resolvedAt").isNotEmpty());
    }

    @Test
    void returnsNotFoundForNonexistentIncident() throws Exception {
        updateIncidentStatus(UUID.randomUUID(), "IDENTIFIED")
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.title").value("Incident not found"));
    }

    @Test
    void rejectsInvalidIncidentStatusTransition() throws Exception {
        Incident incident = createTestIncident();

        updateIncidentStatus(incident.getId(), "MONITORING")
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.status").value(409))
            .andExpect(jsonPath("$.title").value("Invalid incident status transition"));
    }

    // updateIncidentStatus reads the current status, validates the transition, then writes
    // back (check-then-act). Without a version check, two concurrent requests reading the
    // same old status could both pass validation and the second write would silently
    // overwrite the first (lost update) instead of failing. This test proves the @Version
    // field on Incident actually stops that: a write based on a stale version must be
    // rejected with 409, not silently accepted.
    @Test
    void returnsConflictWhenIncidentModifiedConcurrently() throws Exception {
        Incident incident = createTestIncident();

        // Bypass Hibernate and bump the version column directly in the database to
        // simulate "another request already committed a change to this row." The Incident
        // object above stays in this test's persistence context with the old version, so
        // Hibernate has no idea the row moved on underneath it.
        jdbcTemplate.update(
            "UPDATE incidents SET version = version + 1 WHERE id = ?",
            incident.getId()
        );

        updateIncidentStatus(incident.getId(), "IDENTIFIED")
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.status").value(409))
            .andExpect(jsonPath("$.title").value("Incident update conflict"));
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

        // saveAndFlush (not save) so the row actually exists in the database by the time
        // this method returns. returnsConflictWhenIncidentModifiedConcurrently() updates
        // this row directly via JDBC right after calling this method; with plain save(),
        // the insert would still be pending and that JDBC update would silently match zero
        // rows.
        return incidentRepository.saveAndFlush(
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

    private ResultActions updateIncidentStatus(
        UUID incidentId,
        String status
    ) throws Exception {
        UpdateIncidentStatusRequest request = new UpdateIncidentStatusRequest(status);

        return mockMvc.perform(
            patch("/api/incidents/{incidentId}/status", incidentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        );
    }
}
