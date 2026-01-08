package com.studenthub.business.clients;

public record AuthValidateResponse(
        Long userId,
        String role
) {}