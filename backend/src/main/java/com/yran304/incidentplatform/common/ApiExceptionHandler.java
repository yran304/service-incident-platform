package com.yran304.incidentplatform.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.yran304.incidentplatform.organizations.OrganizationSlugAlreadyExistsException;

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
}
