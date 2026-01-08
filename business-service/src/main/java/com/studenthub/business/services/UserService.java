package com.studenthub.business.services;

import com.studenthub.business.dtos.UserProfileResponse;
import com.studenthub.business.dtos.UserUpdateRequest;
import com.studenthub.business.entities.UserProfile;
import com.studenthub.business.enums.TeacherRequestStatus;
import com.studenthub.business.repositories.UserProfileRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.time.Instant;
import java.util.Optional;

@Service
public class UserService {

    private final UserProfileRepository userProfileRepository;

    public UserService(UserProfileRepository userProfileRepository) {
        this.userProfileRepository = userProfileRepository;
    }

    // -------------------------
    // Teacher request workflow
    // -------------------------
    @Transactional
    public void requestTeacherRole(String note) {
        UserProfile profile = getOrCreateCurrentProfile();

        // IMPORTANT: rolul nu mai vine din DB; îl iei din token
        if (hasRole("TEACHER")) {
            throw new RuntimeException("Already a teacher");
        }

        if (profile.getTeacherRequestStatus() == TeacherRequestStatus.PENDING) {
            throw new RuntimeException("Request already pending");
        }

        profile.setTeacherRequestStatus(TeacherRequestStatus.PENDING);
        profile.setTeacherRequestedAt(Instant.now());
        profile.setTeacherRequestNote(note);

        // @Transactional -> auto flush
    }

    // -------------------------
    // Profile / Read operations
    // -------------------------
    public UserProfileResponse getMyProfile() {
        UserProfile profile = getOrCreateCurrentProfile();
        return toProfileResponse(profile);
    }

    public UserProfileResponse getUserProfileById(Long userId) throws AccessDeniedException {
        Long currentUserId = getCurrentUserId();

        // policy:
        // - admin vede pe oricine
        // - user vede doar pe el
        if (!hasRole("ADMIN") && !currentUserId.equals(userId)) {
            throw new AccessDeniedException("Not allowed");
        }

        UserProfile profile = userProfileRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User profile not found with id: " + userId));

        return toProfileResponse(profile);
    }

    /**
     * List profiles with search + pagination.
     * ADMIN only.
     *
     * IMPORTANT: search nu mai poate căuta email (email e în auth-service).
     * Caută doar după nume (dacă ai `name` în UserProfile).
     */
    public Page<UserProfileResponse> getUsers(String search, Pageable pageable) throws AccessDeniedException {
        if (!hasRole("ADMIN")) {
            throw new AccessDeniedException("Only admins can list users");
        }

        Page<UserProfile> page;
        if (search == null || search.trim().isBlank()) {
            page = userProfileRepository.findAll(pageable);
        } else {
            String q = search.trim();
            // schimbă repo method: findByNameContainingIgnoreCase(String, Pageable)
            page = userProfileRepository.findByNameContainingIgnoreCase(q, pageable);
        }

        return page.map(this::toProfileResponse);
    }

    // -------------------------
    // Update operations
    // -------------------------
    @Transactional
    public UserProfileResponse updateMyProfile(UserUpdateRequest request) {
        UserProfile profile = getOrCreateCurrentProfile();

        if (request.name() != null) {
            profile.setName(request.name().trim());
        }

        // parola NU se gestionează în business-service
        if (request.password() != null) {
            throw new RuntimeException("Password is managed by auth-service");
        }

        return toProfileResponse(profile);
    }

    /**
     * Admin update for another user profile (business fields only).
     */
    @Transactional
    public UserProfileResponse adminUpdateUser(Long userId, UserUpdateRequest request) throws AccessDeniedException {
        if (!hasRole("ADMIN")) {
            throw new AccessDeniedException("Only admins can update other users");
        }

        UserProfile profile = userProfileRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User profile not found with id: " + userId));

        if (request.name() != null) {
            profile.setName(request.name().trim());
        }

        if (request.password() != null) {
            throw new RuntimeException("Password is managed by auth-service");
        }

        return toProfileResponse(profile);
    }

    // -------------------------
    // Delete operations
    // -------------------------
    @Transactional
    public void deleteMyAccount() {
        Long userId = getCurrentUserId();
        // în business ștergi doar profilul business; contul real îl ștergi în auth-service
        userProfileRepository.deleteById(userId);
    }

    @Transactional
    public void adminDeleteUser(Long userId) throws AccessDeniedException {
        if (!hasRole("ADMIN")) {
            throw new AccessDeniedException("Only admins can delete users");
        }

        if (!userProfileRepository.existsById(userId)) {
            throw new RuntimeException("User profile not found with id: " + userId);
        }

        userProfileRepository.deleteById(userId);
    }

    // -------------------------
    // Helpers
    // -------------------------
    private UserProfile getOrCreateCurrentProfile() {
        Long userId = getCurrentUserId();

        Optional<UserProfile> existing = userProfileRepository.findById(userId);
        if (existing.isPresent()) {
            return existing.get();
        }

        // creezi automat profilul la primul request
        UserProfile created = new UserProfile();
        created.setId(userId);
        created.setTeacherRequestStatus(TeacherRequestStatus.NONE); // sau null, după model
        return userProfileRepository.save(created);
    }

    public Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Unauthenticated");
        }

        try {
            // important: filtrul tău trebuie să seteze authentication.getName() = userId (sub)
            return Long.parseLong(authentication.getName());
        } catch (NumberFormatException ex) {
            throw new RuntimeException("Invalid subject (expected userId)");
        }
    }

    public boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) return false;

        String expected = "ROLE_" + role;
        for (GrantedAuthority ga : authentication.getAuthorities()) {
            if (expected.equals(ga.getAuthority())) return true;
        }
        return false;
    }

    private UserProfileResponse toProfileResponse(UserProfile p) {
        return new UserProfileResponse(
                p.getId(),
                p.getName(),
                // email/role nu mai există în business-service
                p.getTeacherRequestStatus(),
                p.getTeacherRequestedAt(),
                p.getTeacherReviewedAt(),
                p.getTeacherReviewNote(),
                p.getTeacherRequestNote()
        );
    }
}
