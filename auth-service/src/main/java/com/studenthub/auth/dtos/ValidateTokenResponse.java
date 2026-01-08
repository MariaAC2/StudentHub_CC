package com.studenthub.auth.dtos;

public record ValidateTokenResponse(
        Long userId,
        String role
) {}