package com.pennywise.auth;

import com.pennywise.common.ApiResponse;
import com.pennywise.security.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/sign-up")
    public ResponseEntity<ApiResponse<AuthDTO.AuthResponse>> signUp(
            @Valid @RequestBody AuthDTO.SignUpRequest request) {
        AuthDTO.AuthResponse response = authService.signUp(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "User registered successfully"));
    }

    @PostMapping("/sign-in")
    public ResponseEntity<ApiResponse<AuthDTO.AuthResponse>> signIn(
            @Valid @RequestBody AuthDTO.SignInRequest request) {
        AuthDTO.AuthResponse response = authService.signIn(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Sign-in successful"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthDTO.RefreshTokenResponse>> refresh(
            @Valid @RequestBody AuthDTO.RefreshTokenRequest request) {
        AuthDTO.RefreshTokenResponse response = authService.refresh(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Token refreshed"));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<AuthDTO.UserProfileResponse>> getCurrentUser(
            @AuthenticatedUser Long userId) {
        AuthDTO.UserProfileResponse response = authService.getUserProfile(userId);
        return ResponseEntity.ok(ApiResponse.success(response, "Profile retrieved"));
    }
}
