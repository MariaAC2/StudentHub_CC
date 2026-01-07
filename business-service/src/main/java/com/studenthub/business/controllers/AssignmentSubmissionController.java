package com.studenthub.business.controllers;

import com.studenthub.business.dtos.AssignmentSubmissionRequest;
import com.studenthub.business.dtos.AssignmentSubmissionResponse;
import com.studenthub.business.dtos.GradeSubmissionRequest;
import com.studenthub.business.services.AssignmentSubmissionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping
public class AssignmentSubmissionController {

    private final AssignmentSubmissionService submissionService;

    public AssignmentSubmissionController(AssignmentSubmissionService submissionService) {
        this.submissionService = submissionService;
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/assignments/{assignmentId}/submissions")
    public AssignmentSubmissionResponse createSubmission(
            @PathVariable Long assignmentId,
            @RequestBody AssignmentSubmissionRequest request
    ) {
        return submissionService.createSubmission(assignmentId, request);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/submissions/{id}")
    public AssignmentSubmissionResponse getSubmission(@PathVariable Long id) {
        return submissionService.getSubmission(id);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/assignments/{assignmentId}/submissions")
    public Page<AssignmentSubmissionResponse> listForAssignment(
            @PathVariable Long assignmentId,
            Pageable pageable
    ) {
        return submissionService.listSubmissionsForAssignment(assignmentId, pageable);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/me/submissions")
    public Page<AssignmentSubmissionResponse> listMySubmissions(Pageable pageable) {
        return submissionService.listMySubmissions(pageable);
    }

    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/submissions/{id}")
    public AssignmentSubmissionResponse updateSubmission(
            @PathVariable Long id,
            @RequestBody AssignmentSubmissionRequest request
    ) {
        return submissionService.updateSubmission(id, request);
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/submissions/{id}/submit")
    public AssignmentSubmissionResponse submitSubmission(@PathVariable Long id) {
        return submissionService.submitSubmission(id);
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/submissions/{id}/grade")
    public AssignmentSubmissionResponse gradeSubmission(
            @PathVariable Long id,
            @RequestBody GradeSubmissionRequest request
    ) {
        return submissionService.gradeSubmission(id, request);
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/submissions/{id}")
    public void deleteSubmission(@PathVariable Long id) {
        submissionService.deleteSubmission(id);
    }
}
