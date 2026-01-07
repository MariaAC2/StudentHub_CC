package com.studenthub.business.dtos;

import com.studenthub.business.enums.TeacherRequestStatus;
import com.studenthub.business.enums.UserRole;

import java.time.Instant;

public record UserProfileResponse(
        Long id,
        String name,
        String email,
        UserRole role,
        TeacherRequestStatus teacherRequestStatus,
        Instant teacherRequestedAt,
        Instant teacherReviewedAt,
        String teacherReviewNote,
        String teacherRequestNote
) {}
