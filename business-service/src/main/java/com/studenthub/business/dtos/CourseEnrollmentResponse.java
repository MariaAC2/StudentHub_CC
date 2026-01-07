package com.studenthub.business.dtos;

public record CourseEnrollmentResponse(Long courseId, String courseName, String instructorName, String role, String status) {
}
