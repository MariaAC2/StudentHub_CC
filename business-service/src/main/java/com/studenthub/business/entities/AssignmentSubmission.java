package com.studenthub.business.entities;

import com.studenthub.business.enums.SubmissionStatus;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(
        name = "assignment_submissions",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_assignment_student_attempt",
                columnNames = {"assignment_id", "student_id", "attempt_no"}
        )
)
public class AssignmentSubmission {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assignment_id", nullable = false)
    private Assignment assignment;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(name = "attempt_no", nullable = false)
    private int attemptNo = 1; // if you don’t want attempts, keep always 1

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SubmissionStatus status = SubmissionStatus.DRAFT; // DRAFT/SUBMITTED/GRADED/RETURNED

    @Column(columnDefinition = "text")
    private String textAnswer;

    private Instant submittedAt;

    // ---- grading ----
    private Integer gradePoints;              // null until graded
    private Instant gradedAt;

    @Column(name = "graded_by_id")
    private Long gradedById;                    // teacher/admin

    @Column(columnDefinition = "text")
    private String feedback;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Assignment getAssignment() {
        return assignment;
    }

    public void setAssignment(Assignment assignment) {
        this.assignment = assignment;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public int getAttemptNo() {
        return attemptNo;
    }

    public void setAttemptNo(int attemptNo) {
        this.attemptNo = attemptNo;
    }

    public SubmissionStatus getStatus() {
        return status;
    }

    public void setStatus(SubmissionStatus status) {
        this.status = status;
    }

    public String getTextAnswer() {
        return textAnswer;
    }

    public void setTextAnswer(String textAnswer) {
        this.textAnswer = textAnswer;
    }

    public Instant getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(Instant submittedAt) {
        this.submittedAt = submittedAt;
    }

    public Integer getGradePoints() {
        return gradePoints;
    }

    public void setGradePoints(Integer gradePoints) {
        this.gradePoints = gradePoints;
    }

    public Instant getGradedAt() {
        return gradedAt;
    }

    public void setGradedAt(Instant gradedAt) {
        this.gradedAt = gradedAt;
    }

    public Long getGradedById() {
        return gradedById;
    }

    public void setGradedById(Long gradedById) {
        this.gradedById = gradedById;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }
}
