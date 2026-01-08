package com.studenthub.auth.controllers;

import com.studenthub.auth.dtos.LoginRequest;
import com.studenthub.auth.dtos.RegisterRequest;
import com.studenthub.auth.dtos.TokenResponse;
import com.studenthub.auth.dtos.ValidateTokenResponse;
import com.studenthub.auth.services.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public void register(@RequestBody RegisterRequest request) {
        authService.register(request);
    }

    @PostMapping("/login")
    public TokenResponse login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/validate")
    public ValidateTokenResponse validate(Authentication authentication) {

        Jwt jwt = (Jwt) authentication.getPrincipal();

        assert jwt != null;
        Long userId = Long.valueOf(jwt.getSubject());
        String role = jwt.getClaim("roles");

        return new ValidateTokenResponse(userId, role);
    }
}
