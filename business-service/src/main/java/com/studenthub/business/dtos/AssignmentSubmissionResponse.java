package com.studenthub.business.dtos;

import java.time.Instant;

public record AssignmentSubmissionResponse(
        Long id,
        Long assignmentId,
        Long studentId,
        int attemptNo,
        String status,
        String textAnswer,
        Instant submittedAt,
        Integer gradePoints,
        Instant gradedAt,
        Long gradedById,
        String feedback
) {}
