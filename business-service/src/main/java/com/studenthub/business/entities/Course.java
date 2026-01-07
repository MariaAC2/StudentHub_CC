package com.studenthub.business.entities;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(
        name = "courses",
        indexes = {
                @Index(name = "idx_courses_parent", columnList = "parent_course_id"),
                @Index(name = "idx_courses_created_by", columnList = "created_by_id")
        }
)
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "text")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_course_id")
    private Course parentCourse;

    @OneToMany(mappedBy = "parentCourse", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC, id ASC")
    private List<Course> subCourses = new ArrayList<>();

    @Column(name="sort_order")
    private Integer sortOrder;

    // Who created it (usually teacher)
    @Column(name = "created_by_id", nullable = false)
    private Long createdById;

    @Column(nullable=false)
    private boolean active = true;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CourseEnrollment> enrollments = new HashSet<>();

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("dueAt ASC, id ASC")
    private List<Assignment> assignments = new ArrayList<>();

    public boolean isMainCourse() {
        return parentCourse == null;
    }

    public void addSubCourse(Course child) {
        child.setParentCourse(this);
        this.subCourses.add(child);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Course getParentCourse() {
        return parentCourse;
    }

    public void setParentCourse(Course parentCourse) {
        this.parentCourse = parentCourse;
    }

    public List<Course> getSubCourses() {
        return subCourses;
    }

    public void setSubCourses(List<Course> subCourses) {
        this.subCourses = subCourses;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Long getCreatedById() {
        return createdById;
    }

    public void setCreatedById(Long createdById) {
        this.createdById = createdById;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Set<CourseEnrollment> getEnrollments() {
        return enrollments;
    }

    public void setEnrollments(Set<CourseEnrollment> enrollments) {
        this.enrollments = enrollments;
    }

    public List<Assignment> getAssignments() {
        return assignments;
    }

    public void setAssignments(List<Assignment> assignments) {
        this.assignments = assignments;
    }
}
