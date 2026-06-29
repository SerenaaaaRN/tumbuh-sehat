package com.nutricare.service.impl;

import com.nutricare.TestDataFactory;
import com.nutricare.domain.entity.User;
import com.nutricare.domain.enums.Role;
import com.nutricare.exception.DuplicateResourceException;
import com.nutricare.exception.ResourceNotFoundException;
import com.nutricare.dto.request.auth.LoginRequest;
import com.nutricare.dto.request.auth.RefreshTokenRequest;
import com.nutricare.dto.request.auth.RegisterRequest;
import com.nutricare.repository.RefreshTokenRepository;
import com.nutricare.repository.UserRepository;
import com.nutricare.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtil jwtUtil;
    @Mock private AuthenticationManager authenticationManager;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, refreshTokenRepository, passwordEncoder, jwtUtil, authenticationManager);
        // Inject refreshExpirationMs via reflection
        java.lang.reflect.Field field;
        try {
            field = AuthService.class.getDeclaredField("refreshExpirationMs");
            field.setAccessible(true);
            field.set(authService, 604800000L);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void register_shouldSucceed() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("new@test.com");
        request.setPassword("password123");
        request.setName("User Baru");

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$hash");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            return User.builder().id(u.getId()).email(u.getEmail()).name(u.getName()).role(u.getRole()).isActive(true).build();
        });
        when(jwtUtil.generateAccessToken(anyString(), anyString(), any(Role.class))).thenReturn("access-token");
        when(jwtUtil.generateRefreshToken(anyString(), anyString())).thenReturn("refresh-token");

        var response = authService.register(request);

        assertNotNull(response);
        assertEquals("access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());
        assertEquals("User Baru", response.getUser().getName());
        assertEquals(Role.PARENT, response.getUser().getRole());
        verify(userRepository).existsByEmail("new@test.com");
    }

    @Test
    void register_shouldThrow_whenEmailExists() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("existing@test.com");
        request.setPassword("password123");
        request.setName("User Existing");

        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> authService.register(request));
    }

    @Test
    void login_shouldSucceed() {
        LoginRequest request = new LoginRequest();
        request.setEmail("parent@test.com");
        request.setPassword("password123");

        User user = TestDataFactory.createParent();

        when(authenticationManager.authenticate(any())).thenReturn(null);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(jwtUtil.generateAccessToken(anyString(), anyString(), any(Role.class))).thenReturn("access-token");
        when(jwtUtil.generateRefreshToken(anyString(), anyString())).thenReturn("refresh-token");

        var response = authService.login(request);

        assertNotNull(response);
        assertEquals("access-token", response.getAccessToken());
        assertEquals("parent@test.com", response.getUser().getEmail());
    }

    @Test
    void login_shouldThrow_whenInvalidCredentials() {
        LoginRequest request = new LoginRequest();
        request.setEmail("wrong@test.com");
        request.setPassword("wrong");

        when(authenticationManager.authenticate(any()))
            .thenThrow(new org.springframework.security.authentication.BadCredentialsException("Bad credentials"));

        assertThrows(org.springframework.security.authentication.BadCredentialsException.class,
            () -> authService.login(request));
    }

    @Test
    void refresh_shouldSucceed() {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("valid-refresh-token");

        User user = TestDataFactory.createParent();
        var refreshToken = TestDataFactory.createRefreshToken(user);

        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(refreshToken));
        when(jwtUtil.generateAccessToken(anyString(), anyString(), any(Role.class))).thenReturn("new-access-token");
        when(jwtUtil.generateRefreshToken(anyString(), anyString())).thenReturn("new-refresh-token");

        var response = authService.refresh(request);

        assertNotNull(response);
        assertEquals("new-access-token", response.getAccessToken());
        assertTrue(refreshToken.getRevoked());
    }

    @Test
    void refresh_shouldThrow_whenInvalidToken() {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("invalid-token");

        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> authService.refresh(request));
    }

    @Test
    void logout_shouldRevokeToken() {
        User user = TestDataFactory.createParent();
        var refreshToken = TestDataFactory.createRefreshToken(user);

        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(refreshToken));

        authService.logout("valid-token");

        assertTrue(refreshToken.getRevoked());
        verify(refreshTokenRepository).save(refreshToken);
    }

    @Test
    void getMe_shouldSucceed() {
        User user = TestDataFactory.createParent();

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        var response = authService.getMe(user.getId());

        assertNotNull(response);
        assertEquals(user.getName(), response.getName());
        assertEquals(user.getEmail(), response.getEmail());
        assertEquals(Role.PARENT, response.getRole());
    }

    @Test
    void getMe_shouldThrow_whenNotFound() {
        when(userRepository.findById(anyString())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> authService.getMe("nonexistent"));
    }
}
