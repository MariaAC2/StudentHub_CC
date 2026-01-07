package com.studenthub.business.controllers;

import com.studenthub.business.dtos.AssignmentRequest;
import com.studenthub.business.dtos.AssignmentResponse;
import com.studenthub.business.services.AssignmentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;

@RestController
@RequestMapping
public class AssignmentController {

    private final AssignmentService assignmentService;

    public AssignmentController(AssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    // -------------------------
    // Create assignment (teacher/admin enforced in service)
    // -------------------------
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/assignments")
    public AssignmentResponse createAssignment(@RequestBody AssignmentRequest request) {
        return assignmentService.createAssignment(request);
    }

    // -------------------------
    // Get assignment by id
    // -------------------------
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/assignments/{id}")
    public AssignmentResponse getAssignmentById(@PathVariable Long id) {
        return assignmentService.getAssignmentById(id);
    }

    // -------------------------
    // List assignments per course (paged + optional title search)
    // GET /courses/{courseId}/assignments?title=lab&page=0&size=10&sort=dueAt,asc
    // -------------------------
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/courses/{courseId}/assignments")
    public Page<AssignmentResponse> listAssignmentsForCourse(
            @PathVariable Long courseId,
            @RequestParam(required = false) String title,
            Pageable pageable
    ) {
        return assignmentService.getAssignmentsForCourse(courseId, title, pageable);
    }

    // -------------------------
    // Update assignment (teacher/admin enforced in service)
    // -------------------------
    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/assignments/{id}")
    public AssignmentResponse updateAssignment(
            @PathVariable Long id,
            @RequestBody AssignmentRequest request
    ) throws AccessDeniedException {
        return assignmentService.updateAssignment(id, request);
    }

    // -------------------------
    // Delete assignment (teacher/admin + ownership enforced in service)
    // -------------------------
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/assignments/{id}")
    public void deleteAssignment(@PathVariable Long id) throws AccessDeniedException {
        assignmentService.deleteAssignment(id);
    }
}
