package com.studenthub.auth.dtos;

public record RegisterRequest(
        String email,
        String password,
        String role
) {}