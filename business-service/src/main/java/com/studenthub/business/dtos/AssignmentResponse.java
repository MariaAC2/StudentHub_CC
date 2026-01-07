package com.studenthub.business.dtos;

import java.time.OffsetDateTime;

public record AssignmentResponse(
        Long id,
        Long courseId,
        String title,
        String description,
        OffsetDateTime openAt,
        OffsetDateTime dueAt,
        OffsetDateTime closeAt,
        Integer maxPoints
) {}
