package com.studenthub.business.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.studenthub.business.entities.Course;

public interface CourseRepository extends JpaRepository<Course, Long> {
    Page<Course> findByActiveTrueAndParentCourseIsNull(Pageable pageable);
    Page<Course> findByActiveTrueAndParentCourseIsNullAndTitleContainingIgnoreCase(
            String title,
            Pageable pageable
    );
}