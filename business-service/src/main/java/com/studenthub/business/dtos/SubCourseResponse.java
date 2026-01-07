package com.studenthub.business.dtos;

public record SubCourseResponse(
        Long id,
        String title,
        boolean active,
        Integer sortOrder
) {
}
