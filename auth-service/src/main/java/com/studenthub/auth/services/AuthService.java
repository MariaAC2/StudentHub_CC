package com.studenthub.auth.services;

import com.studenthub.auth.dtos.LoginRequest;
import com.studenthub.auth.dtos.RegisterRequest;
import com.studenthub.auth.dtos.TokenResponse;
import com.studenthub.auth.dtos.TokenResponse;
import com.studenthub.auth.entities.User;
import com.studenthub.auth.enums.UserRole;
import com.studenthub.auth.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;

    @Value("${security.jwt.issuer}")
    private String issuer;

    @Value("${security.jwt.expiration-seconds:3600}")
    private long expirationSeconds;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtEncoder jwtEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtEncoder = jwtEncoder;
    }

    @Transactional
    public TokenResponse register(RegisterRequest request) {
        if (request == null) throw new RuntimeException("Request is null");

        String email = request.email() == null ? null : request.email().trim().toLowerCase();
        String password = request.password();

        if (email == null || email.isBlank()) throw new RuntimeException("Email is required");
        if (password == null || password.isBlank()) throw new RuntimeException("Password is required");

        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already registered");
        }

        // Security recommendation: do NOT allow self-assign ADMIN/TEACHER
        // If you want to accept role from business-service, restrict it:
        UserRole role = UserRole.STUDENT;
        if (request.role() != null && !request.role().isBlank()) {
            UserRole requested = UserRole.valueOf(request.role().trim().toUpperCase());
            if (requested == UserRole.STUDENT) {
                role = requested;
            }
            // else ignore / throw, depending on your preference
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);

        User saved = userRepository.save(user);

        String token = generateToken(saved);
        return new TokenResponse(saved.getId(), token);
    }

    public String login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email().trim().toLowerCase())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        return generateToken(user);
    }

    private String generateToken(User user) {
        Instant now = Instant.now();

        String role = user.getRole().name(); // ADMIN / TEACHER / STUDENT

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .issuedAt(now)
                .expiresAt(now.plus(Duration.ofDays(7))) // 7 days
                .subject(user.getId().toString())        // sub = user id
                .claim("role", role)
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims))
                .getTokenValue();
    }
}
