package com.studenthub.business.dtos;

public record AuthRegisterRequest(
        String email,
        String password,
        String role
) {}
