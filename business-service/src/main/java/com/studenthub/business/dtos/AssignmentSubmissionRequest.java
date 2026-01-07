package com.studenthub.business.dtos;

/**
 * Request DTO for creating/updating an assignment submission.
 * Clients send the submission text and optionally an attempt number.
 */
public record AssignmentSubmissionRequest(
        Integer attemptNo,
        String textAnswer
) {}

