package com.student_hub.services;

import com.student_hub.dtos.LoginRequest;
import com.student_hub.dtos.RegisterRequest;
import com.student_hub.dtos.TokenResponse;
import com.student_hub.entities.User;
import com.student_hub.enums.UserRole;
import com.student_hub.repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class AuthService {

    private final JwtEncoder jwtEncoder;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(JwtEncoder jwtEncoder,
                       UserRepository userRepository,
                       PasswordEncoder passwordEncoder) {
        this.jwtEncoder = jwtEncoder;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void register(RegisterRequest request) {

        String email = request.email().trim().toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already registered");
        }

        User user = new User(request.name().trim(), email, passwordEncoder.encode(request.password()));

        userRepository.save(user);
    }

    public TokenResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        String token = jwtEncoder.encode(
                JwtEncoderParameters.from(
                        JwsHeader.with(MacAlgorithm.HS256).build(),
                        JwtClaimsSet.builder()
                                .subject(user.getId().toString())
                                .claim("roles", user.getRole().name())
                                .issuedAt(Instant.now())
                                .expiresAt(Instant.now().plusSeconds(604800)) // 7 days
                                .build()
                )
        ).getTokenValue();

        return new TokenResponse(token);
    }
}
