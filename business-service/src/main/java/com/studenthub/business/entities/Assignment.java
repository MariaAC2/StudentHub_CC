package com.studenthub.business.entities;

import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(
        name = "assignments",
        indexes = {
                @Index(name = "idx_asg_course", columnList = "course_id"),
                @Index(name = "idx_asg_due", columnList = "due_at")
        }
)
public class Assignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name="course_id", nullable=false)
    private Course course;

    @Column(nullable=false)
    private String title;

    @Column(columnDefinition = "text")
    private String description;

    @Column(name="open_at")
    private OffsetDateTime openAt;

    @Column(name="due_at")
    private OffsetDateTime dueAt;

    @Column(name="close_at")
    private OffsetDateTime closeAt;

    @Column(name="max_points")
    private Integer maxPoints;

    protected Assignment() {}

    public Assignment(
            String title,
            String description,
            OffsetDateTime openAt,
            OffsetDateTime dueAt,
            OffsetDateTime closeAt,
            Integer maxPoints,
            Course course
    ) {
        this.title = title;
        this.description = description;
        this.openAt = openAt;
        this.dueAt = dueAt;
        this.closeAt = closeAt;
        this.maxPoints = maxPoints;
        this.course = course;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public OffsetDateTime getOpenAt() {
        return openAt;
    }

    public void setOpenAt(OffsetDateTime openAt) {
        this.openAt = openAt;
    }

    public OffsetDateTime getDueAt() {
        return dueAt;
    }

    public void setDueAt(OffsetDateTime dueAt) {
        this.dueAt = dueAt;
    }

    public OffsetDateTime getCloseAt() {
        return closeAt;
    }

    public void setCloseAt(OffsetDateTime closeAt) {
        this.closeAt = closeAt;
    }

    public Integer getMaxPoints() {
        return maxPoints;
    }

    public void setMaxPoints(Integer maxPoints) {
        this.maxPoints = maxPoints;
    }
}
