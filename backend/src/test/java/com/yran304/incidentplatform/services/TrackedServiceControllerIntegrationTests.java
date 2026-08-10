package com.yran304.incidentplatform.services;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.transaction.annotation.Transactional;

import com.yran304.incidentplatform.organizations.Organization;
import com.yran304.incidentplatform.organizations.OrganizationRepository;

import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class TrackedServiceControllerIntegrationTests {
    
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Test
    void createsService() throws Exception {
        Organization organization = organizationRepository.save(
            new Organization(
                UUID.randomUUID(), 
                "Test Organization", 
                "test-organization-" + UUID.randomUUID(), 
                Instant.now()
            )
        );

        CreateServiceRequest request = new CreateServiceRequest(
            "Payment API",
            "payment-api"
        );

        mockMvc.perform(
            post("/api/organizations/{organizationId}/services", organization.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)
            )
        )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").isNotEmpty())
            .andExpect(jsonPath("$.organizationId").value(organization.getId().toString()))
            .andExpect(jsonPath("$.name").value("Payment API"))
            .andExpect(jsonPath("$.slug").value("payment-api"))
            .andExpect(jsonPath("$.currentStatus").value("OPERATIONAL"));            
    }

    @Test
    void rejectsInvalidServiceRequest() throws Exception {
        CreateServiceRequest request = new CreateServiceRequest(
            "",                 // invalid: @NotBlank name
            "invalid slug"      // invalid: contains a space, violating @Pattern
        );
        
        mockMvc.perform(
            post("/api/organizations/{organizationId}/services", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsDuplicateServiceSlugWithinOrganization() throws Exception {
        Organization organization = organizationRepository.save(
            new Organization(
                UUID.randomUUID(),
                "Test Organization",
                "test-organization-" + UUID.randomUUID(),
                Instant.now()
            )
        );

        CreateServiceRequest request = new CreateServiceRequest(
            "Payment API",
            "payment-api"
        );

        mockMvc.perform(
            post("/api/organizations/{organizationId}/services", organization.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated());

        mockMvc.perform(
            post("/api/organizations/{organizationId}/services", organization.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.status").value(409))
            .andExpect(jsonPath("$.title").value("Service slug already exists"));
    }

    @Test
    void listsServicesForOrganization() throws Exception {
        Organization organization = organizationRepository.save(
            new Organization(
                UUID.randomUUID(),
                "Test Organization",
                "test-organization-" + UUID.randomUUID(),
                Instant.now()
            )
        );

        String slug = "listed-service-" + UUID.randomUUID();
        CreateServiceRequest request = new CreateServiceRequest(
            "Listed Service",
            slug
        );

        mockMvc.perform(
            post("/api/organizations/{organizationId}/services", organization.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated());

        mockMvc.perform(
            get("/api/organizations/{organizationId}/services", organization.getId())
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[?(@.slug == '%s')]", slug).exists());
    }

    @Test
    void returnsNotFoundForNonexistentOrganization() throws Exception {
        mockMvc.perform(
            get("/api/organizations/{organizationId}/services", UUID.randomUUID())
        )
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.title").value("Organization not found"));
    }
}
