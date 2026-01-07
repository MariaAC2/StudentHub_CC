package com.studenthub.business.services;

import com.studenthub.business.dtos.UserProfileResponse;
import com.studenthub.business.dtos.UserUpdateRequest;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.studenthub.business.entities.User;
import com.studenthub.business.enums.TeacherRequestStatus;
import com.studenthub.business.enums.UserRole;
import com.studenthub.business.repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.time.Instant;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // -------------------------
    // Teacher request workflow
    // -------------------------
    @Transactional
    public void requestTeacherRole(String note) {
        User user = getCurrentUser();

        if (user.getRole() == UserRole.TEACHER) {
            throw new RuntimeException("Already a teacher");
        }

        if (user.getTeacherRequestStatus() == TeacherRequestStatus.PENDING) {
            throw new RuntimeException("Request already pending");
        }

        user.setTeacherRequestStatus(TeacherRequestStatus.PENDING);
        user.setTeacherRequestedAt(Instant.now());
        user.setTeacherRequestNote(note);

        // no need to call save() explicitly in @Transactional, but it's fine if you do
    }

    // -------------------------
    // Profile / Read operations
    // -------------------------
    public UserProfileResponse getMyProfile() {
        User user = getCurrentUser();
        return toProfileResponse(user);
    }

    public UserProfileResponse getUserProfileById(Long userId) throws AccessDeniedException {
        User current = getCurrentUser();

        // If you want ONLY admins to view other users:
        // if (current.getRole() != UserRole.ADMIN && !current.getId().equals(userId)) {
        //     throw new AccessDeniedException("Not allowed");
        // }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        // A sensible default policy:
        // - admin can view anyone
        // - user can view themselves
        if (current.getRole() != UserRole.ADMIN && !current.getId().equals(user.getId())) {
            throw new AccessDeniedException("Not allowed");
        }

        return toProfileResponse(user);
    }

    /**
     * List users with search + pagination.
     * Recommended: ADMIN only.
     *
     * search can match name or email.
     */
    public Page<UserProfileResponse> getUsers(String search, Pageable pageable) throws AccessDeniedException {
        User current = getCurrentUser();

        if (current.getRole() != UserRole.ADMIN) {
            throw new AccessDeniedException("Only admins can list users");
        }

        Page<User> page;
        if (search == null || search.trim().isBlank()) {
            page = userRepository.findAll(pageable);
        } else {
            String q = search.trim();
            page = userRepository.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(q, q, pageable);
        }

        return page.map(this::toProfileResponse);
    }

    // -------------------------
    // Update operations
    // -------------------------
    @Transactional
    public UserProfileResponse updateMyProfile(UserUpdateRequest request) {
        User user = getCurrentUser();

        if (request.name() != null) {
            user.setName(request.name().trim());
        }

        if (request.password() != null) {
            user.setPassword(passwordEncoder.encode(request.password()));
        }

        return toProfileResponse(user);
    }

    /**
     * Admin update for another user.
     * Keep this separate from "my profile" updates to avoid privilege bugs.
     */
    @Transactional
    public UserProfileResponse adminUpdateUser(Long userId, UserUpdateRequest request) throws AccessDeniedException {
        User current = getCurrentUser();

        if (current.getRole() != UserRole.ADMIN) {
            throw new AccessDeniedException("Only admins can update other users");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        if (request.name() != null) {
            user.setName(request.name().trim());
        }

        if (request.password() != null) {
            user.setPassword(passwordEncoder.encode(request.password()));
        }

        return toProfileResponse(user);
    }

    // -------------------------
    // Delete operations
    // -------------------------
    @Transactional
    public void deleteMyAccount() {
        User user = getCurrentUser();
        userRepository.delete(user);
    }

    @Transactional
    public void adminDeleteUser(Long userId) throws AccessDeniedException {
        User current = getCurrentUser();

        if (current.getRole() != UserRole.ADMIN) {
            throw new AccessDeniedException("Only admins can delete users");
        }

        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found with id: " + userId);
        }

        userRepository.deleteById(userId);
    }

    // -------------------------
    // Current user helper
    // -------------------------
    User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Unauthenticated");
        }

        Long userId;
        try {
            userId = Long.parseLong(authentication.getName()); // subject = userId
        } catch (NumberFormatException ex) {
            throw new RuntimeException("Invalid token subject (expected userId)");
        }

        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private UserProfileResponse toProfileResponse(User u) {
        return new UserProfileResponse(
                u.getId(),
                u.getName(),
                u.getEmail(),
                u.getRole(),
                u.getTeacherRequestStatus(),
                u.getTeacherRequestedAt(),
                u.getTeacherReviewedAt(),
                u.getTeacherReviewNote(),
                u.getTeacherRequestNote()
        );
    }
}

