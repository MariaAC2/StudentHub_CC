// Java
package com.studenthub.business.services;

import com.studenthub.business.dtos.CourseEnrollmentRequest;
import com.studenthub.business.entities.Course;
import com.studenthub.business.entities.CourseEnrollment;
import com.studenthub.business.entities.UserProfile;
import com.studenthub.business.enums.EnrollmentStatus;
import com.studenthub.business.enums.UserRole;
import com.studenthub.business.repositories.CourseEnrollmentRepository;
import com.studenthub.business.repositories.CourseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CourseEnrollmentService {

    private final CourseEnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final UserService userService;

    public CourseEnrollmentService(CourseEnrollmentRepository enrollmentRepository,
                                   CourseRepository courseRepository,
                                   UserService userService) {
        this.enrollmentRepository = enrollmentRepository;
        this.courseRepository = courseRepository;
        this.userService = userService;
    }

    public CourseEnrollment createEnrollment(CourseEnrollmentRequest request) {
        Long courseId = request.courseId();
        Long currentUserId = userService.getCurrentUserId();

        if (!userService.hasRole("STUDENT")) {
            throw new RuntimeException("Only students can enroll in courses.");
        }

        if (enrollmentRepository.existsByCourseIdAndUserId(courseId, currentUserId)) {
            throw new RuntimeException("User is already enrolled in this course.");
        }

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found with id: " + courseId));

        CourseEnrollment enrollment = new CourseEnrollment(course, currentUserId, request.role());

        return enrollmentRepository.save(enrollment);
    }

    public CourseEnrollment getEnrollmentById(Long id) {
        return enrollmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Enrollment not found with id: " + id));
    }

    public List<CourseEnrollment> getAllEnrollments() {
        return enrollmentRepository.findAll();
    }

    public List<CourseEnrollment> getEnrollmentsByCourseId(Long courseId) {
        return enrollmentRepository.findByCourseId(courseId);
    }


    @Transactional
    public CourseEnrollment updateEnrollment(Long id, String status) {
        CourseEnrollment enrollment = enrollmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Enrollment not found with id: " + id));

        try {
            EnrollmentStatus parsed = EnrollmentStatus.valueOf(status.trim().toUpperCase());
            enrollment.setStatus(parsed);
        } catch (IllegalArgumentException | NullPointerException ex) {
            throw new RuntimeException("Invalid enrollment status: " + status);
        }

        return enrollmentRepository.save(enrollment);
    }

    public void deleteEnrollment(Long id) {
        if (!enrollmentRepository.existsById(id)) {
            throw new RuntimeException("Enrollment not found with id: " + id);
        }
        enrollmentRepository.deleteById(id);
    }
}
