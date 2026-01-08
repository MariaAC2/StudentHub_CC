package com.studenthub.business.dtos;

public record RegisterRequest(String name, String email, String password, boolean requestTeacher) {}
