// File: student_hub/src/main/java/com/student_hub/dtos/AssignmentRequest.java
package com.studenthub.business.dtos;

import java.time.OffsetDateTime;

public record AssignmentRequest(
        Long courseId,
        String title,
        String description,
        OffsetDateTime openAt,
        OffsetDateTime dueAt,
        OffsetDateTime closeAt,
        Integer maxPoints
) {}
