package com.yran304.incidentplatform.incidents;

import java.util.UUID;

public class IncidentUpdateConflictException extends RuntimeException {

    public IncidentUpdateConflictException(UUID incidentId) {
        super(
            "Incident " + incidentId
                + " was modified concurrently by another request; please retry"
        );
    }
}
