package com.studenthub.business.repositories;

import com.studenthub.business.entities.AssignmentSubmission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AssignmentSubmissionRepository extends JpaRepository<AssignmentSubmission, Long> {
    Page<AssignmentSubmission> findByAssignmentId(Long assignmentId, Pageable pageable);
    Page<AssignmentSubmission> findByStudentId(Long studentId, Pageable pageable);
    Optional<AssignmentSubmission> findByAssignmentIdAndStudentIdAndAttemptNo(Long assignmentId, Long studentId, int attemptNo);
}

