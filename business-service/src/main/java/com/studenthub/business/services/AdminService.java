package com.studenthub.business.services;

import com.studenthub.business.entities.User;
import com.studenthub.business.enums.TeacherRequestStatus;
import com.studenthub.business.enums.UserRole;
import com.studenthub.business.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class AdminService {

    private final UserRepository userRepository;

    public AdminService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void approveTeacher(Long userId, String note) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getTeacherRequestStatus() != TeacherRequestStatus.PENDING) {
            throw new RuntimeException("No pending request for this user");
        }

        user.setRole(UserRole.TEACHER);
        user.setTeacherRequestStatus(TeacherRequestStatus.APPROVED);
        user.setTeacherReviewedAt(Instant.now());
        user.setTeacherReviewNote(note);

        userRepository.save(user);
    }

    public void rejectTeacher(Long userId, String note) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getTeacherRequestStatus() != TeacherRequestStatus.PENDING) {
            throw new RuntimeException("No pending request for this user");
        }

        user.setTeacherRequestStatus(TeacherRequestStatus.REJECTED);
        user.setTeacherReviewedAt(Instant.now());
        user.setTeacherReviewNote(note);

        userRepository.save(user);
    }
}

