package com.studenthub.business.entities;

import com.studenthub.business.enums.TeacherRequestStatus;
import com.studenthub.business.enums.UserRole;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "users")
public class UserProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TeacherRequestStatus teacherRequestStatus = TeacherRequestStatus.NONE;

    private Instant teacherRequestedAt;
    private Instant teacherReviewedAt;
    private String teacherReviewNote;
    private String teacherRequestNote;

    public UserProfile() {}

    public UserProfile(String name, String email, String password) {
        this.name = name;
    }

    public UserProfile(String name, String email, String password, UserRole role) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TeacherRequestStatus getTeacherRequestStatus() {
        return teacherRequestStatus;
    }

    public void setTeacherRequestStatus(TeacherRequestStatus teacherRequestStatus) {
        this.teacherRequestStatus = teacherRequestStatus;
    }

    public Instant getTeacherRequestedAt() {
        return teacherRequestedAt;
    }

    public void setTeacherRequestedAt(Instant teacherRequestedAt) {
        this.teacherRequestedAt = teacherRequestedAt;
    }

    public Instant getTeacherReviewedAt() {
        return teacherReviewedAt;
    }

    public void setTeacherReviewedAt(Instant teacherReviewedAt) {
        this.teacherReviewedAt = teacherReviewedAt;
    }

    public String getTeacherRequestNote() {
        return teacherRequestNote;
    }

    public void setTeacherRequestNote(String teacherReviewNote) {
        this.teacherRequestNote = teacherReviewNote;
    }

    public String getTeacherReviewNote() {
        return teacherReviewNote;
    }

    public void setTeacherReviewNote(String teacherReviewNote) {
        this.teacherReviewNote = teacherReviewNote;
    }
}
