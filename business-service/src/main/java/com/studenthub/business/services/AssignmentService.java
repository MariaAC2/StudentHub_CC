package com.studenthub.business.services;

import com.studenthub.business.dtos.AssignmentRequest;
import com.studenthub.business.dtos.AssignmentResponse;
import com.studenthub.business.entities.UserProfile;
import com.studenthub.business.enums.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.studenthub.business.entities.Assignment;
import com.studenthub.business.entities.Course;
import com.studenthub.business.repositories.AssignmentRepository;
import com.studenthub.business.repositories.CourseRepository;

import java.nio.file.AccessDeniedException;
import java.time.OffsetDateTime;
import java.util.List;

@Service
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final CourseRepository courseRepository;
    private final UserService userService; // assuming you have a UserService to get current user

    public AssignmentService(AssignmentRepository assignmentRepository, CourseRepository courseRepository, UserService userService) {
        this.assignmentRepository = assignmentRepository;
        this.courseRepository = courseRepository;
        this.userService = userService;
    }

    public AssignmentResponse createAssignment(AssignmentRequest assignment) {
        Long currentUserId = userService.getCurrentUserId();

        // 1) enforce permission
        if (!userService.hasRole("TEACHER") && !userService.hasRole("ADMIN")) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Only teachers can create assignments"
            );
        }

        Course course = courseRepository.findById(assignment.courseId())
                .orElseThrow(() -> new RuntimeException(
                        "Course not found with id: " + assignment.courseId()
                ));

        if (!course.isMainCourse()) {
            throw new RuntimeException("Assignments can be created only for main courses");
        }

        Assignment newAssignment = new Assignment(
                assignment.title(),
                assignment.description(),
                assignment.openAt(),
                assignment.dueAt(),
                assignment.closeAt(),
                assignment.maxPoints(),
                course
        );

        Assignment saved = assignmentRepository.save(newAssignment);

        return new AssignmentResponse(
                saved.getId(),
                saved.getCourse().getId(),
                saved.getTitle(),
                saved.getDescription(),
                saved.getOpenAt(),
                saved.getDueAt(),
                saved.getCloseAt(),
                saved.getMaxPoints()
        );
    }

    public AssignmentResponse getAssignmentById(Long id) {
        Assignment a = assignmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Assignment not found with id: " + id));

        return new AssignmentResponse(
                a.getId(),
                a.getCourse().getId(),
                a.getTitle(),
                a.getDescription(),
                a.getOpenAt(),
                a.getDueAt(),
                a.getCloseAt(),
                a.getMaxPoints()
        );
    }

    public Page<AssignmentResponse> getAssignmentsForCourse(Long courseId, String title, Pageable pageable) {

        // Ensure course exists (optional but usually nice)
        courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found with id: " + courseId));

        Page<Assignment> page;

        if (title == null || title.trim().isBlank()) {
            page = assignmentRepository.findByCourseId(courseId, pageable);
        } else {
            page = assignmentRepository.findByCourseIdAndTitleContainingIgnoreCase(
                    courseId,
                    title.trim(),
                    pageable
            );
        }

        return page.map(a -> new AssignmentResponse(
                a.getId(),
                a.getCourse().getId(),
                a.getTitle(),
                a.getDescription(),
                a.getOpenAt(),
                a.getDueAt(),
                a.getCloseAt(),
                a.getMaxPoints()
        ));
    }

    public List<AssignmentResponse> getAllAssignments() {
        List<Assignment> list = assignmentRepository.findAll();

        return list.stream().map(a -> new AssignmentResponse(
                a.getId(),
                a.getCourse().getId(),
                a.getTitle(),
                a.getDescription(),
                a.getOpenAt(),
                a.getDueAt(),
                a.getCloseAt(),
                a.getMaxPoints()
        )).toList();
    }

    public Page<Assignment> getAssignmentsByCourseId(Long courseId, Pageable pageable) {
        return assignmentRepository.findByCourseId(courseId, pageable);
    }

    @Transactional
    public AssignmentResponse updateAssignment(Long id, AssignmentRequest updated) throws AccessDeniedException {
        Long currentUserId = userService.getCurrentUserId();

        if (!userService.hasRole("TEACHER") && !userService.hasRole("ADMIN")) {
            throw new AccessDeniedException("Only teachers can update assignments");
        }

        Assignment existing = assignmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Assignment not found with id: " + id));

        if (updated.title() != null) {
            existing.setTitle(updated.title().trim());
        }
        if (updated.description() != null) {
            existing.setDescription(updated.description());
        }
        if (updated.openAt() != null) {
            existing.setOpenAt(OffsetDateTime.from(updated.openAt()));
        }
        if (updated.dueAt() != null) {
            existing.setDueAt(OffsetDateTime.from(updated.dueAt()));
        }
        if (updated.closeAt() != null) {
            existing.setCloseAt(OffsetDateTime.from(updated.closeAt()));
        }
        if (updated.maxPoints() != null) {
            existing.setMaxPoints(updated.maxPoints());
        }

        if (updated.courseId() != null) {
            Course course = courseRepository.findById(updated.courseId())
                    .orElseThrow(() -> new RuntimeException("Course not found with id: " + updated.courseId()));
            existing.setCourse(course);
        }

        return new AssignmentResponse(
                existing.getId(),
                existing.getCourse().getId(),
                existing.getTitle(),
                existing.getDescription(),
                existing.getOpenAt(),
                existing.getDueAt(),
                existing.getCloseAt(),
                existing.getMaxPoints()
        );
    }

    @Transactional
    public void deleteAssignment(Long id) throws AccessDeniedException {

        Long currentUserId = userService.getCurrentUserId();

        if (!userService.hasRole("TEACHER") && !userService.hasRole("ADMIN")) {
            throw new AccessDeniedException("Only teachers can delete assignments");
        }

        Assignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Assignment not found with id: " + id)
                );

        Course course = assignment.getCourse();
        Long courseCreatorId = course.getCreatedById();

        boolean isCreator = courseCreatorId.equals(currentUserId);
        boolean isAdmin = userService.hasRole("ADMIN");

        if (!isCreator && !isAdmin) {
            throw new AccessDeniedException(
                    "You can delete assignments only from your own courses"
            );
        }

        assignmentRepository.delete(assignment);
    }
}
