package com.studenthub.business.dtos;

public record RegisterResponse(
        String token,
        UserProfileResponse profile
) {}
