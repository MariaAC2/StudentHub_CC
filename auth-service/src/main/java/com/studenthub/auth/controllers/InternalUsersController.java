package com.studenthub.auth.controllers;

import com.studenthub.auth.dtos.RegisterRequest;
import com.studenthub.auth.dtos.TokenResponse;
import com.studenthub.auth.entities.User;
import com.studenthub.auth.enums.UserRole;
import com.studenthub.auth.repositories.UserRepository;
import com.studenthub.auth.services.AuthService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth/internal")
public class InternalUsersController {
    @Value("${internal.secret}")
    private String internalSecret;

    private final UserRepository userRepository;
    private final AuthService authService;

    public InternalUsersController(UserRepository userRepository, AuthService authService) {
        this.userRepository = userRepository;
        this.authService = authService;
    }

    @PostMapping("/register")
    public TokenResponse registerWithRole(
            @RequestBody RegisterRequest request,
            @RequestHeader("X-INTERNAL-SECRET") String secret
    ) {
        if (secret == null || !secret.equals(internalSecret)) {
            throw new RuntimeException("Forbidden");
        }
        return authService.registerWithRole(request); // you add this method in auth
    }

    @PatchMapping("/users/{id}/role")
    public void setRole(@PathVariable Long id, @RequestBody SetRoleRequest req) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setRole(UserRole.valueOf(req.role().toUpperCase()));
        userRepository.save(user);
    }

    @PutMapping("/users/{id}/role")
    public void updateRole(
            @PathVariable Long id,
            @RequestParam String role,
            @RequestHeader("X-INTERNAL-SECRET") String secret
    ) {
        if (secret == null || !secret.equals(internalSecret)) {
            throw new RuntimeException("Forbidden");
        }
        authService.updateRole(id, role); // you add this too
    }

    public record SetRoleRequest(String role) {}
}
