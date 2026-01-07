package com.studenthub.business.dtos;

public record GradeSubmissionRequest(
        Integer gradePoints,
        String feedback
) {}

