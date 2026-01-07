package com.studenthub.business.dtos;

import java.util.List;

public record CourseDetailResponse(
        Long id,
        String title,
        String description,
        boolean active,
        Long parentCourseId,
        Integer sortOrder,
        List<SubCourseResponse> subCourses
) {}
