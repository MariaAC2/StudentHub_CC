package com.studenthub.business.services;

import com.studenthub.business.dtos.AssignmentSubmissionRequest;
import com.studenthub.business.dtos.AssignmentSubmissionResponse;
import com.studenthub.business.dtos.GradeSubmissionRequest;
import com.studenthub.business.entities.Assignment;
import com.studenthub.business.entities.AssignmentSubmission;
import com.studenthub.business.entities.Course;
import com.studenthub.business.entities.UserProfile;
import com.studenthub.business.enums.SubmissionStatus;
import com.studenthub.business.enums.UserRole;
import com.studenthub.business.repositories.AssignmentRepository;
import com.studenthub.business.repositories.AssignmentSubmissionRepository;
import com.studenthub.business.repositories.CourseEnrollmentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
public class AssignmentSubmissionService {

    private final AssignmentSubmissionRepository submissionRepository;
    private final AssignmentRepository assignmentRepository;
    private final CourseEnrollmentRepository enrollmentRepository;
    private final UserService userService;

    public AssignmentSubmissionService(
            AssignmentSubmissionRepository submissionRepository,
            AssignmentRepository assignmentRepository,
            CourseEnrollmentRepository enrollmentRepository,
            UserService userService
    ) {
        this.submissionRepository = submissionRepository;
        this.assignmentRepository = assignmentRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.userService = userService;
    }

    @Transactional
    public AssignmentSubmissionResponse createSubmission(Long assignmentId, AssignmentSubmissionRequest req) {
        Long currentUserId = userService.getCurrentUserId();

        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found with id: " + assignmentId));

        // only students can create submissions (teachers/admins can not submit as students)
        if (!userService.hasRole("STUDENT")) {
            throw new AccessDeniedException("Only students can create submissions");
        }

        Course course = assignment.getCourse();

        // check enrollment
        boolean enrolled = enrollmentRepository.existsByCourseIdAndUserId(course.getId(), currentUserId);
        if (!enrolled) {
            throw new AccessDeniedException("You must be enrolled in the course to submit assignments");
        }

        // check open/close windows
        Instant now = Instant.now();
        if (assignment.getOpenAt() != null && now.isBefore(assignment.getOpenAt().toInstant())) {
            throw new RuntimeException("Assignment not yet open");
        }
        if (assignment.getCloseAt() != null && now.isAfter(assignment.getCloseAt().toInstant())) {
            throw new RuntimeException("Assignment closed for submissions");
        }

        int attemptNo = req.attemptNo() != null ? req.attemptNo() : 1;

        // ensure unique attempt
        Optional<AssignmentSubmission> existingAttempt = submissionRepository
                .findByAssignmentIdAndStudentIdAndAttemptNo(assignmentId, currentUserId, attemptNo);
        if (existingAttempt.isPresent()) {
            throw new RuntimeException("Submission attempt already exists for attemptNo=" + attemptNo);
        }

        AssignmentSubmission s = new AssignmentSubmission();
        s.setAssignment(assignment);
        s.setStudentId(currentUserId);
        s.setAttemptNo(attemptNo);
        s.setTextAnswer(req.textAnswer());
        s.setStatus(SubmissionStatus.DRAFT);

        AssignmentSubmission saved = submissionRepository.save(s);

        return toResponse(saved);
    }

    public AssignmentSubmissionResponse getSubmission(Long id) throws AccessDeniedException {
        Long currentUserId = userService.getCurrentUserId();

        AssignmentSubmission s = submissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Submission not found with id: " + id));

        // owner or teacher/admin of the course can view
        if (s.getStudentId().equals(currentUserId)) {
            return toResponse(s);
        }

        // teacher/admin: check if they are teacher of the course or admin
        Course course = s.getAssignment().getCourse();
        boolean isAdmin = userService.hasRole("ADMIN");
        boolean isCourseCreator = course.getCreatedById() != null && course.getCreatedById().equals(currentUserId);

        if (!isAdmin && !isCourseCreator) {
            throw new AccessDeniedException("Not allowed to view this submission");
        }

        return toResponse(s);
    }

    public Page<AssignmentSubmissionResponse> listSubmissionsForAssignment(Long assignmentId, Pageable pageable) throws AccessDeniedException {
        Long currentUserId = userService.getCurrentUserId();
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found with id: " + assignmentId));

        Course course = assignment.getCourse();
        boolean isAdmin = userService.hasRole("ADMIN");
        boolean isCourseCreator = course.getCreatedById() != null && course.getCreatedById().equals(currentUserId);

        if (!isAdmin && !isCourseCreator) {
            throw new AccessDeniedException("Only course teachers or admins can list submissions");
        }

        Page<AssignmentSubmission> page = submissionRepository.findByAssignmentId(assignmentId, pageable);
        return page.map(this::toResponse);
    }

    public Page<AssignmentSubmissionResponse> listMySubmissions(Pageable pageable) {
        Long currentUserId = userService.getCurrentUserId();
        Page<AssignmentSubmission> page = submissionRepository.findByStudentId(currentUserId, pageable);
        return page.map(this::toResponse);
    }

    @Transactional
    public AssignmentSubmissionResponse updateSubmission(Long id, AssignmentSubmissionRequest req) throws AccessDeniedException {
        Long currentUserId = userService.getCurrentUserId();

        AssignmentSubmission s = submissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Submission not found with id: " + id));

        if (!s.getStudentId().equals(currentUserId)) {
            throw new AccessDeniedException("Only submission owner can update the submission");
        }

        if (s.getStatus() != SubmissionStatus.DRAFT) {
            throw new RuntimeException("Only DRAFT submissions can be updated");
        }

        if (req.textAnswer() != null) {
            s.setTextAnswer(req.textAnswer());
        }

        if (req.attemptNo() != null && req.attemptNo() != s.getAttemptNo()) {
            // check uniqueness
            Optional<AssignmentSubmission> existing = submissionRepository
                    .findByAssignmentIdAndStudentIdAndAttemptNo(s.getAssignment().getId(), currentUserId, req.attemptNo());
            if (existing.isPresent()) {
                throw new RuntimeException("Another submission exists with attemptNo=" + req.attemptNo());
            }
            s.setAttemptNo(req.attemptNo());
        }

        return toResponse(s);
    }

    @Transactional
    public AssignmentSubmissionResponse submitSubmission(Long id) throws AccessDeniedException {
        Long currentUserId = userService.getCurrentUserId();

        AssignmentSubmission s = submissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Submission not found with id: " + id));

        if (!s.getStudentId().equals(currentUserId)) {
            throw new AccessDeniedException("Only submission owner can submit");
        }

        if (s.getStatus() != SubmissionStatus.DRAFT) {
            throw new RuntimeException("Only DRAFT submissions can be submitted");
        }

        Assignment assignment = s.getAssignment();
        Instant now = Instant.now();
        if (assignment.getOpenAt() != null && now.isBefore(assignment.getOpenAt().toInstant())) {
            throw new RuntimeException("Assignment not yet open");
        }
        if (assignment.getCloseAt() != null && now.isAfter(assignment.getCloseAt().toInstant())) {
            throw new RuntimeException("Assignment closed for submissions");
        }

        if ((s.getTextAnswer() == null || s.getTextAnswer().isBlank())) {
            throw new RuntimeException("Cannot submit empty answer");
        }

        s.setStatus(SubmissionStatus.SUBMITTED);
        s.setSubmittedAt(Instant.now());

        return toResponse(s);
    }

    @Transactional
    public AssignmentSubmissionResponse gradeSubmission(Long id, GradeSubmissionRequest req) throws AccessDeniedException {
        Long currentUserId = userService.getCurrentUserId();

        AssignmentSubmission s = submissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Submission not found with id: " + id));

        Assignment assignment = s.getAssignment();
        Course course = assignment.getCourse();

        boolean isAdmin = userService.hasRole("ADMIN");
        boolean isCourseCreator = course.getCreatedById() != null && course.getCreatedById().equals(currentUserId);

        if (!isAdmin && !isCourseCreator) {
            throw new AccessDeniedException("Only course teachers or admins can grade submissions");
        }

        if (req.gradePoints() != null) {
            if (assignment.getMaxPoints() != null && req.gradePoints() > assignment.getMaxPoints()) {
                throw new RuntimeException("Grade exceeds assignment max points");
            }
            s.setGradePoints(req.gradePoints());
        }

        if (req.feedback() != null) {
            s.setFeedback(req.feedback());
        }

        s.setGradedAt(Instant.now());
        s.setGradedById(currentUserId);
        s.setStatus(SubmissionStatus.GRADED);

        return toResponse(s);
    }

    @Transactional
    public void deleteSubmission(Long id) throws AccessDeniedException {
        Long currentUserId = userService.getCurrentUserId();
        AssignmentSubmission s = submissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Submission not found with id: " + id));

        if (s.getStudentId().equals(currentUserId)) {
            // owner can delete only DRAFT
            if (s.getStatus() != SubmissionStatus.DRAFT) {
                throw new RuntimeException("Only DRAFT submissions can be deleted by owner");
            }
            submissionRepository.delete(s);
            return;
        }

        // admins or course creators can delete any
        Assignment assignment = s.getAssignment();
        boolean isAdmin = userService.hasRole("ADMIN");
        boolean isCourseCreator = assignment.getCourse().getCreatedById() != null && assignment.getCourse().getCreatedById().equals(currentUserId);

        if (!isAdmin && !isCourseCreator) {
            throw new AccessDeniedException("Not allowed to delete this submission");
        }

        submissionRepository.delete(s);
    }

    private AssignmentSubmissionResponse toResponse(AssignmentSubmission s) {
        Long gradedById = s.getGradedById();
        Instant submittedAt = s.getSubmittedAt();
        Instant gradedAt = s.getGradedAt();

        return new AssignmentSubmissionResponse(
                s.getId(),
                s.getAssignment().getId(),
                s.getStudentId(),
                s.getAttemptNo(),
                s.getStatus().name(),
                s.getTextAnswer(),
                submittedAt,
                s.getGradePoints(),
                gradedAt,
                gradedById,
                s.getFeedback()
        );
    }
}
