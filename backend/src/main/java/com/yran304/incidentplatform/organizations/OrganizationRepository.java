package com.yran304.incidentplatform.organizations;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizationRepository extends JpaRepository<Organization, UUID> { // the Java database-access interface for Organization rows.
    // exists -> true or false; By -> start filering by a field; Slug -> use the entity field named 'slug' - a naming convention so Spring data JPA knows what it wants
    boolean existsBySlug(String slug); 
}
