package com.studenthub.business.dtos;

public record CourseResponse(
        Long id,
        String title,
        String description,
        boolean active,
        Long parentCourseId,
        Integer sortOrder,
        int subCourseCount
) {
}
