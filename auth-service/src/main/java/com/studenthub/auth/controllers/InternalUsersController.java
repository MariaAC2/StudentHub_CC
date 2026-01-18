package com.studenthub.auth.controllers;

import com.studenthub.auth.entities.User;
import com.studenthub.auth.enums.UserRole;
import com.studenthub.auth.repositories.UserRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth/internal")
public class InternalUsersController {

    private final UserRepository userRepository;

    public InternalUsersController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PatchMapping("/users/{id}/role")
    public void setRole(@PathVariable Long id, @RequestBody SetRoleRequest req) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setRole(UserRole.valueOf(req.role().toUpperCase()));
        userRepository.save(user);
    }

    public record SetRoleRequest(String role) {}
}
