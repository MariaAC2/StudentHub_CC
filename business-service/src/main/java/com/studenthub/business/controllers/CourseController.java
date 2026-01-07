package com.studenthub.business.controllers;

import com.studenthub.business.dtos.CourseDetailResponse;
import com.studenthub.business.dtos.CourseRequest;
import com.studenthub.business.dtos.CourseResponse;
import com.studenthub.business.dtos.CourseUpdateRequest;
import com.studenthub.business.services.CourseService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;

@RestController
@RequestMapping("/courses")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    // CREATE (teacher/admin only)
    @PreAuthorize("isAuthenticated()")
    @PostMapping
    public CourseResponse create(@RequestBody CourseRequest request) {
        return courseService.createCourse(request);
    }

    // READ one (authenticated or public – your choice)
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    public CourseDetailResponse getById(@PathVariable Long id) {
        return courseService.getCourseById(id);
    }

    // READ all / search
    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public Page<CourseResponse> listActiveMainCourses(
            @RequestParam(required = false) String title,
            Pageable pageable
    ) {
        return courseService.searchActiveMainCourses(title, pageable);
    }

    // UPDATE (teacher/admin + owner check should be in service)
    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/{id}")
    public CourseResponse update(@PathVariable Long id, @RequestBody CourseUpdateRequest request) throws AccessDeniedException {
        return courseService.updateCourse(id, request);
    }

    // DELETE (teacher/admin + owner check should be in service)
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) throws AccessDeniedException {
        courseService.deleteCourse(id);
    }
}
