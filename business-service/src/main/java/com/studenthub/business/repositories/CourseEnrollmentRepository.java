// Java
package com.studenthub.business.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.studenthub.business.entities.CourseEnrollment;

import java.util.List;

public interface CourseEnrollmentRepository extends JpaRepository<CourseEnrollment, Long> {
    List<CourseEnrollment> findByCourseId(Long courseId);
    List<CourseEnrollment> findByUserId(Long userId);
    boolean existsByCourseIdAndUserId(Long courseId, Long userId);
}
