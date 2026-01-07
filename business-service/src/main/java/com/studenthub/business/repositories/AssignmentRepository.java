package com.studenthub.business.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import com.studenthub.business.entities.Assignment;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    Page<Assignment> findByCourseId(Long courseId, Pageable pageable);
    Page<Assignment> findByCourseIdAndTitleContainingIgnoreCase(
            Long courseId,
            String title,
            Pageable pageable
    );
}
