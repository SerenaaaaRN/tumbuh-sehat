package com.nutricare.controller;

import com.nutricare.domain.entity.User;
import com.nutricare.dto.request.auth.LoginRequest;
import com.nutricare.dto.request.auth.RefreshTokenRequest;
import com.nutricare.dto.request.auth.RegisterRequest;
import com.nutricare.dto.response.auth.AuthResponse;
import com.nutricare.service.impl.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * AuthController — /api/auth/**
 * BE-101: POST /api/auth/register
 * BE-102: POST /api/auth/login
 * BE-103: POST /api/auth/refresh
 * BE-104: POST /api/auth/logout
 * BE-105: GET  /api/auth/me
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refresh(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(@RequestBody RefreshTokenRequest request) {
        authService.logout(request.getRefreshToken());
        return ResponseEntity.ok(Map.of("message", "Logout berhasil"));
    }

    @GetMapping("/me")
    public ResponseEntity<AuthResponse.UserResponse> me(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(authService.getMe(user.getId()));
    }
}
