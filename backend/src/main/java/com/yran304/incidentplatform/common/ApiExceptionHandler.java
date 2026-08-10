package com.yran304.incidentplatform.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.yran304.incidentplatform.incidents.IncidentNotFoundException;
import com.yran304.incidentplatform.incidents.InvalidIncidentStatusTransitionException;
import com.yran304.incidentplatform.organizations.OrganizationNotFoundException;
import com.yran304.incidentplatform.organizations.OrganizationSlugAlreadyExistsException;
import com.yran304.incidentplatform.services.ServiceSlugAlreadyExistsException;
import com.yran304.incidentplatform.services.TrackedServiceNotFoundException;

@RestControllerAdvice
public class ApiExceptionHandler {
    
    @ExceptionHandler(OrganizationSlugAlreadyExistsException.class)
    public ResponseEntity<ProblemDetail> handleDuplicateOrganizationSlug(
        OrganizationSlugAlreadyExistsException exception
    ) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.CONFLICT, // if OrganizationSlugAlreadyExistsException, return 409 Conflict
            exception.getMessage()
        );

        problem.setTitle("Organization slug already exists");

        return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
    }

    @ExceptionHandler(OrganizationNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleOrganizationNotFound(
        OrganizationNotFoundException exception
    ) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.NOT_FOUND, 
            exception.getMessage()
        );

        problem.setTitle("Organization not found");

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
    }

    @ExceptionHandler(ServiceSlugAlreadyExistsException.class)
    public ResponseEntity<ProblemDetail> handleDuplicateServiceSlug(
        ServiceSlugAlreadyExistsException exception
    ) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.CONFLICT, 
            exception.getMessage()
        );

        problem.setTitle("Service slug already exists");

        return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
    }

    @ExceptionHandler(TrackedServiceNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleTrackedServiceNotFound(
        TrackedServiceNotFoundException exception
    ) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.NOT_FOUND,
            exception.getMessage()
        );

        problem.setTitle("Service not found");

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
    }

    @ExceptionHandler(IncidentNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleIncidentNotFound(
        IncidentNotFoundException exception
    ) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.NOT_FOUND,
            exception.getMessage()
        );

        problem.setTitle("Incident not found");

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
    }

    @ExceptionHandler(InvalidIncidentStatusTransitionException.class)
    public ResponseEntity<ProblemDetail> handleInvalidIncidentStatusTransition(
        InvalidIncidentStatusTransitionException exception
    ) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.CONFLICT,
            exception.getMessage()
        );

        problem.setTitle("Invalid incident status transition");

        return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
    }

}
