package com.studenthub.business.dtos;

public record UserUpdateRequest(
        String name,
        String password
) {}
