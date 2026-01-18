package com.studenthub.business.services;

import com.studenthub.business.clients.AuthServiceClient;
import com.studenthub.business.dtos.*;
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
    private final AuthServiceClient authServiceClient;

    public UserService(UserProfileRepository userProfileRepository, AuthServiceClient authServiceClient) {
        this.userProfileRepository = userProfileRepository;
        this.authServiceClient = authServiceClient;
    }

    @Transactional
    public RegisterResponse register(RegisterRequest req) {

        System.out.println("=== [REGISTER] START ===");
        System.out.println("[REGISTER] email = " + req.email());
        System.out.println("[REGISTER] requestTeacher = " + req.requestTeacher());

        // 1) validate
        System.out.println("[REGISTER] Step 1 - validation passed");

        // 2) call auth-service register
        System.out.println("[REGISTER] Step 2 - calling auth-service");
        AuthRegisterResponse auth =
                authServiceClient.register(req.email(), req.password(), "STUDENT");

        System.out.println("[REGISTER] Auth-service returned id = " + auth.id());

        // 3) create profile
        System.out.println("[REGISTER] Step 3 - creating user profile");

        UserProfile profile = new UserProfile();
        profile.setId(auth.id());
        profile.setName(req.name());

        if (req.requestTeacher()) {
            System.out.println("[REGISTER] Teacher requested → setting PENDING");
            profile.setTeacherRequestStatus(TeacherRequestStatus.PENDING);
            profile.setTeacherRequestedAt(Instant.now());
        } else {
            System.out.println("[REGISTER] No teacher request");
            profile.setTeacherRequestStatus(TeacherRequestStatus.NONE);
        }

        try {
            System.out.println("[REGISTER] Step 4 - saving profile");
            userProfileRepository.save(profile);
            System.out.println("[REGISTER] Profile saved successfully");
        } catch (Exception e) {
            System.out.println("[REGISTER] ERROR saving profile → compensating");
            System.out.println("[REGISTER] Deleting auth user id = " + auth.id());

            authServiceClient.deleteUser(auth.id());
            throw e;
        }

        System.out.println("[REGISTER] Step 5 - returning response");
        System.out.println("=== [REGISTER] END ===");

        return new RegisterResponse(auth.token(), profileToDto(profile));
    }

    @Transactional
    public void registerWithRole(RegisterRequest req, String role) {

        System.out.println("=== [REGISTER_WITH_ROLE] START ===");
        System.out.println("[REGISTER_WITH_ROLE] email = " + req.email());
        System.out.println("[REGISTER_WITH_ROLE] role = " + role);
        System.out.println("[REGISTER_WITH_ROLE] requestTeacher = " + req.requestTeacher());

        // 1) validate (same as your register; keep it minimal here)
        if (req == null) throw new IllegalArgumentException("RegisterRequest is null");
        if (req.email() == null || req.email().isBlank()) throw new IllegalArgumentException("Email is required");
        if (req.password() == null || req.password().isBlank()) throw new IllegalArgumentException("Password is required");
        if (role == null || role.isBlank()) role = "STUDENT";

        System.out.println("[REGISTER_WITH_ROLE] Step 1 - validation passed");

        // 2) call auth-service register with provided role
        System.out.println("[REGISTER_WITH_ROLE] Step 2 - calling auth-service");
        AuthRegisterResponse auth =
                authServiceClient.register(req.email(), req.password(), role);

        System.out.println("[REGISTER_WITH_ROLE] Auth-service returned id = " + auth.id());

        // 3) create profile
        System.out.println("[REGISTER_WITH_ROLE] Step 3 - creating user profile");

        UserProfile profile = new UserProfile();
        profile.setId(auth.id());
        profile.setName(req.name());

        if (req.requestTeacher()) {
            System.out.println("[REGISTER_WITH_ROLE] Teacher requested → setting PENDING");
            profile.setTeacherRequestStatus(TeacherRequestStatus.PENDING);
            profile.setTeacherRequestedAt(Instant.now());
        } else {
            System.out.println("[REGISTER_WITH_ROLE] No teacher request");
            profile.setTeacherRequestStatus(TeacherRequestStatus.NONE);
        }

        try {
            System.out.println("[REGISTER_WITH_ROLE] Step 4 - saving profile");
            userProfileRepository.save(profile);
            System.out.println("[REGISTER_WITH_ROLE] Profile saved successfully");
        } catch (Exception e) {
            System.out.println("[REGISTER_WITH_ROLE] ERROR saving profile → compensating");
            System.out.println("[REGISTER_WITH_ROLE] Deleting auth user id = " + auth.id());

            authServiceClient.deleteUser(auth.id());
            throw e;
        }

        System.out.println("[REGISTER_WITH_ROLE] Step 5 - returning response");
        System.out.println("=== [REGISTER_WITH_ROLE] END ===");
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

        return userProfileRepository.findById(userId)
                .orElseGet(() -> {
                    UserProfile created = new UserProfile();
                    created.setId(userId);

                    created.setName("User " + userId);

                    created.setTeacherRequestStatus(TeacherRequestStatus.NONE);

                    try {
                        return userProfileRepository.save(created); // INSERT (because isNew=true)
                    } catch (org.springframework.dao.DataIntegrityViolationException e) {
                        // if two requests race and both try to create, one insert will fail
                        return userProfileRepository.findById(userId).orElseThrow();
                    }
                });
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

    private UserProfileResponse profileToDto(UserProfile p) {
        return new UserProfileResponse(
                p.getId(),
                p.getName(),
                p.getTeacherRequestStatus(),
                p.getTeacherRequestedAt(),
                p.getTeacherReviewedAt(),
                p.getTeacherReviewNote(),
                p.getTeacherRequestNote()
        );
    }
}
