package com.yran304.incidentplatform.services;

import java.util.UUID;

public class TrackedServiceNotFoundException extends RuntimeException {

    public TrackedServiceNotFoundException(UUID serviceId) {
        super("Service with id '" + serviceId + "' was not found");
    }
}
