package com.yran304.incidentplatform.incidentupdates;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface IncidentUpdateRepository extends JpaRepository<IncidentUpdate, UUID> {

    List<IncidentUpdate> findAllByIncidentIdOrderByCreatedAtAsc(UUID incidentId);
}
