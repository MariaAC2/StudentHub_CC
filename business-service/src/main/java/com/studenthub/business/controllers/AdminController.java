package com.studenthub.business.controllers;

import com.studenthub.business.dtos.ApproveTeacherRequest;
import com.studenthub.business.services.AdminService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/teacher-requests/{userId}/approve")
    public void approveTeacher(
            @PathVariable Long userId,
            @RequestBody ApproveTeacherRequest request
    ) {
        adminService.approveTeacher(userId, request.note());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/teacher-requests/{userId}/reject")
    public void rejectTeacher(
            @PathVariable Long userId,
            @RequestBody ApproveTeacherRequest request
    ) {
        adminService.rejectTeacher(userId, request.note());
    }
}

