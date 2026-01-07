package com.studenthub.business.controllers;

import com.studenthub.business.dtos.CourseEnrollmentRequest;
import com.studenthub.business.entities.CourseEnrollment;
import com.studenthub.business.services.CourseEnrollmentService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping
public class CourseEnrollmentController {

    private final CourseEnrollmentService enrollmentService;

    public CourseEnrollmentController(CourseEnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    // Student enrolls in a course
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/enrollments")
    public CourseEnrollment enroll(@RequestBody CourseEnrollmentRequest request) {
        return enrollmentService.createEnrollment(request);
    }

    // Get one enrollment (useful for admin/teacher; later add authorization)
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/enrollments/{id}")
    public CourseEnrollment getById(@PathVariable Long id) {
        return enrollmentService.getEnrollmentById(id);
    }

    // List enrollments for a course (teacher/admin; later add authorization)
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/courses/{courseId}/enrollments")
    public List<CourseEnrollment> listForCourse(@PathVariable Long courseId) {
        return enrollmentService.getEnrollmentsByCourseId(courseId);
    }

    // Update enrollment status (teacher/admin)
    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/enrollments/{id}")
    public CourseEnrollment updateStatus(
            @PathVariable Long id,
            @RequestParam String status
    ) {
        return enrollmentService.updateEnrollment(id, status);
    }

    // Delete enrollment (student cancels or admin removes)
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/enrollments/{id}")
    public void delete(@PathVariable Long id) {
        enrollmentService.deleteEnrollment(id);
    }
}
