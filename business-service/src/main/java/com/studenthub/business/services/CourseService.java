package com.studenthub.business.services;

import java.nio.file.AccessDeniedException;
import java.util.List;

import com.studenthub.business.dtos.*;
import com.studenthub.business.entities.UserProfile;
import com.studenthub.business.enums.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.studenthub.business.entities.Course;
import com.studenthub.business.repositories.CourseRepository;

@Service
public class CourseService {

    private final CourseRepository courseRepository;
    private final UserService userService; // assuming you have a UserService to get current user

    public CourseService(CourseRepository courseRepository, UserService userService) {
        this.courseRepository = courseRepository;
        this.userService = userService;
    }

    private CourseResponse toListItem(Course c) {
        return new CourseResponse(
                c.getId(),
                c.getTitle(),
                c.getDescription(),
                c.isActive(),
                c.getParentCourse() == null ? null : c.getParentCourse().getId(),
                c.getSortOrder(),
                c.getSubCourses() == null ? 0 : c.getSubCourses().size()
        );
    }

    private CourseDetailResponse toDetail(Course c) {
        var subs = c.getSubCourses().stream()
                .map(sc -> new SubCourseResponse(
                        sc.getId(),
                        sc.getTitle(),
                        sc.isActive(),
                        sc.getSortOrder()
                ))
                .toList();

        return new CourseDetailResponse(
                c.getId(),
                c.getTitle(),
                c.getDescription(),
                c.isActive(),
                c.getParentCourse() == null ? null : c.getParentCourse().getId(),
                c.getSortOrder(),
                subs
        );
    }

    public CourseResponse createCourse(CourseRequest request) {
        Long currentUserId = userService.getCurrentUserId();

        // 1) enforce permission: allow TEACHER and ADMIN only
        if (!userService.hasRole("TEACHER") && !userService.hasRole("ADMIN")) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Only teachers or admins can create courses"
            );
        }

        // 2) build entity
        Course course = new Course();
        course.setTitle(request.title().trim());
        course.setDescription(request.description());
        course.setCreatedById(currentUserId);

        // 3) optional parent course
        if (request.parentCourseId() != null) {
            Course parent = courseRepository.findById(request.parentCourseId())
                    .orElseThrow(() -> new RuntimeException("Parent course not found"));

            course.setParentCourse(parent);
            course.setSortOrder(parent.getSubCourses().size() + 1);
        }

        Course newCourse = courseRepository.save(course);

        return toListItem(newCourse);
    }

    public CourseDetailResponse getCourseById(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found with id: " + id));

        return toDetail(course);
    }

    public Page<CourseResponse> searchActiveMainCourses(String title, Pageable pageable) {
        Page<Course> page;

        if (title == null || title.trim().isBlank()) {
            page = courseRepository.findByActiveTrueAndParentCourseIsNull(pageable);
        } else {
            page = courseRepository.findByActiveTrueAndParentCourseIsNullAndTitleContainingIgnoreCase(
                    title.trim(),
                    pageable
            );
        }

        return page.map(this::toListItem);
    }

    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    @Transactional
    public CourseResponse updateCourse(Long id, CourseUpdateRequest updated) throws AccessDeniedException {
        Long currentUserId = userService.getCurrentUserId();

        if (!userService.hasRole("TEACHER") && !userService.hasRole("ADMIN")) {
            throw new AccessDeniedException("Only teachers or admins can update courses");
        }

        Course existing = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found with id: " + id));

        if (!userService.hasRole("ADMIN") &&
                !existing.getCreatedById().equals(currentUserId)) {
            throw new AccessDeniedException("You can update only your own courses");
        }

        if (updated.title() != null) existing.setTitle(updated.title().trim());
        if (updated.description() != null) existing.setDescription(updated.description());
        if (updated.active() != null) existing.setActive(updated.active());
        if (updated.sortOrder() != null) existing.setSortOrder(updated.sortOrder());

        if (updated.parentCourseId() != null) {
            if (updated.parentCourseId().equals(existing.getId())) {
                throw new RuntimeException("A course cannot be its own parent");
            }
            Course parent = courseRepository.findById(updated.parentCourseId())
                    .orElseThrow(() -> new RuntimeException("Parent course not found"));
            existing.setParentCourse(parent);
        }

        return toListItem(existing);
    }

    @Transactional
    public void deleteCourse(Long id) throws AccessDeniedException {
        Long currentUserId = userService.getCurrentUserId();

        if (!userService.hasRole("TEACHER") && !userService.hasRole("ADMIN")) {
            throw new AccessDeniedException("Only teachers or admins can delete courses");
        }

        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found with id: " + id));

        // Only the creator can delete (optionally allow ADMIN too)
        boolean isCreator = course.getCreatedById().equals(currentUserId);
        boolean isAdmin = userService.hasRole("ADMIN");

        if (!isCreator && !isAdmin) {
            throw new AccessDeniedException("You can delete only courses you created");
        }

        courseRepository.delete(course);
    }
}
