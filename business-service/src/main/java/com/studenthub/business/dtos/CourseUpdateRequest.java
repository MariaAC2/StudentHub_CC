package com.studenthub.business.dtos;

public record CourseUpdateRequest(
        String title,
        String description,
        Long parentCourseId,
        Integer sortOrder,
        Boolean active
) {}
