package com.studenthub.auth.dtos;

public record RegisterRequest(String name, String email, String password, boolean requestTeacher) {}
