package com.student_hub.entities;

import com.student_hub.enums.TeacherRequestStatus;
import com.student_hub.enums.UserRole;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role = UserRole.STUDENT;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TeacherRequestStatus teacherRequestStatus = TeacherRequestStatus.NONE;

    private Instant teacherRequestedAt;
    private Instant teacherReviewedAt;
    private String teacherReviewNote;
    private String teacherRequestNote;

    public User() {}

    public User(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public User(String name, String email, String password, UserRole role) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
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
