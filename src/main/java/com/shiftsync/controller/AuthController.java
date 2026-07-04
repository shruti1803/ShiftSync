package com.shiftsync.controller;

import com.shiftsync.dto.request.MockLoginRequest;
import com.shiftsync.dto.response.AuthResponse;
import com.shiftsync.dto.response.UserResponse;
import com.shiftsync.entity.User;
import com.shiftsync.exception.ResourceNotFoundException;
import com.shiftsync.repository.UserRepository;
import com.shiftsync.security.CurrentUserUtil;
import com.shiftsync.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Authentication endpoints (mock login for development)")
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

    @PostMapping("/mock-login")
    @Operation(summary = "Mock login — auto-creates user if not found. Dev/mock profile only.")
    public ResponseEntity<AuthResponse> mockLogin(@Valid @RequestBody MockLoginRequest request) {
        return ResponseEntity.ok(authService.mockLogin(request));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token using a valid refresh token")
    public ResponseEntity<AuthResponse> refresh(@RequestParam String refreshToken) {
        return ResponseEntity.ok(authService.refreshToken(refreshToken));
    }

    @GetMapping("/me")
    @Operation(summary = "Get the currently authenticated user's profile")
    public ResponseEntity<UserResponse> getCurrentUser() {
        Long userId = CurrentUserUtil.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        return ResponseEntity.ok(authService.toUserResponse(user));
    }
}