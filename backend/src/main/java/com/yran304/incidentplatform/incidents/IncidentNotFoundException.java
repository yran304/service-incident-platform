package com.yran304.incidentplatform.incidents;

import java.util.UUID;

public class IncidentNotFoundException extends RuntimeException {

    public IncidentNotFoundException(UUID incidentId) {
        super("Incident with id '" + incidentId + "' was not found");
    }
}
