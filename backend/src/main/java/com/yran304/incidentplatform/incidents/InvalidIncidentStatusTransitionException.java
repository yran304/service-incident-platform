package com.yran304.incidentplatform.incidents;

public class InvalidIncidentStatusTransitionException extends RuntimeException {

    public InvalidIncidentStatusTransitionException(
        String currentStatus,
        String requestedStatus
    ) {
        super(
            "Cannot change incident status from '" + currentStatus
                + "' to '" + requestedStatus + "'"
        );
    }
}
