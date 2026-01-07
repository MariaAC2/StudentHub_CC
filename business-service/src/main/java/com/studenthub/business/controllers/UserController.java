package com.studenthub.business.controllers;

import com.studenthub.business.dtos.RequestTeacherRoleRequest;
import com.studenthub.business.dtos.UserProfileResponse;
import com.studenthub.business.dtos.UserUpdateRequest;
import com.studenthub.business.services.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // View my profile
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/me")
    public UserProfileResponse getMyProfile() {
        return userService.getMyProfile();
    }

    // Update my profile (partial update)
    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/me")
    public UserProfileResponse updateMyProfile(@RequestBody UserUpdateRequest request) {
        return userService.updateMyProfile(request);
    }

    // Request teacher role (user side)
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/me/teacher-request")
    public void requestTeacher(@RequestBody RequestTeacherRoleRequest request) {
        userService.requestTeacherRole(request.note());
    }
}
