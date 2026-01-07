package com.studenthub.business.dtos;

import com.studenthub.business.enums.CourseRole;

public record CourseEnrollmentRequest(Long courseId, CourseRole role) {
}
