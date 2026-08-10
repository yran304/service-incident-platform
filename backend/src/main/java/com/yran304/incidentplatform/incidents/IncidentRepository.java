package com.yran304.incidentplatform.incidents;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface IncidentRepository extends JpaRepository<Incident, UUID> {

    List<Incident> findAllByServiceId(UUID serviceId);
}
