package com.studenthub.business.dtos;

public record CourseRequest(String title, String description, Long parentCourseId) {
}
