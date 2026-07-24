package com.yran304.incidentplatform.organizations;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import org.springframework.transaction.annotation.Transactional;

import tools.jackson.databind.ObjectMapper;

@SpringBootTest // Starts the full Spring Boot application context for the test. Real Spring-managed components such as Controller, Service, Repository, JPA/Flyway configuration, and database connection can participate in the integration test.
@AutoConfigureMockMvc // Configures MockMvc for the test, allowing us to simulate HTTP requests to Spring MVC endpoints without starting a real HTTP server. The requests still go through the real Controller and application layers.
@Transactional // Runs each test inside a database transaction. Database changes made during the test are automatically rolled back afterward, preventing test data from affecting other tests.
public class OrganizationControllerIntegrationTests {
    
    @Autowired // Injects the Spring-managed MockMvc object into this test class, so we can use it without manually creating the object.
    private MockMvc mockMvc; // MockMvc: A Spring MVC testing tool used to simulate HTTP requests and verify HTTP responses without starting a real HTTP server.

    @Autowired
    private ObjectMapper objectMapper; // converts between Java Object ↔ JSON, useful for creating JSON request bodies and reading JSON responses in tests.

    @Test
    void createsOrganization() throws Exception {
        String slug = "test-organization-" + UUID.randomUUID();

        CreateOrganizationRequest request = new CreateOrganizationRequest(
            "Test Organization", 
            slug
        );

        mockMvc.perform(
            post("/api/organizations")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").isNotEmpty())
            .andExpect(jsonPath("$.name").value("Test Organization"))
            .andExpect(jsonPath("$.slug").value(slug));
    }

    @Test
    void rejectsInvalidOrganizationRequest() throws Exception {
        CreateOrganizationRequest request = new CreateOrganizationRequest(
            "", 
            "invalid slug"
        );

        mockMvc.perform(
            post("/api/organizations")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsDuplicateOrganizationSlug() throws Exception {
        String slug = "duplicate-test-" + UUID.randomUUID();

        CreateOrganizationRequest request = new CreateOrganizationRequest(
            "First Organization", 
            slug
        );

        mockMvc.perform(
            post("/api/organizations")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))   
        )
            .andExpect(status().isCreated());
        
        mockMvc.perform(
            post("/api/organizations")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
        ) 
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.status").value(409))
            .andExpect(jsonPath("$.title").value("Organization slug already exists"));  
    }

    @Test
    void listsOrganizations() throws Exception {
        String slug = "list-test-" + UUID.randomUUID();

        CreateOrganizationRequest request = new CreateOrganizationRequest(
            "Listed Organization",
            slug
        );

        mockMvc.perform(
            post("/api/organizations")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated());

        mockMvc.perform(
            get("/api/organizations")
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[?(@.slug == '%s')]", slug).exists());
    }
}
