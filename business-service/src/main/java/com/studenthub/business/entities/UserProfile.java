package com.studenthub.business.entities;

import org.springframework.data.domain.Persistable;
import com.studenthub.business.enums.TeacherRequestStatus;
import com.studenthub.business.enums.UserRole;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "users")
public class UserProfile implements Persistable<Long> {
    @Id
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

    @Transient
    private boolean isNew = true;

    public UserProfile() {}

    public UserProfile(String name, String email, String password) {
        this.name = name;
    }

    public UserProfile(String name, String email, String password, UserRole role) {
        this.name = name;
    }

    @PostLoad
    @PostPersist
    void markNotNew() {
        this.isNew = false;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public boolean isNew() {
        return isNew;
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
