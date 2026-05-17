package com.pennywise.auth;

import java.time.Instant;

public class AuthDTO {

    public record SignUpRequest(
            String name,
            String email,
            String password
    ) {}

    public record SignInRequest(
            String email,
            String password
    ) {}

    public record AuthResponse(
            Long id,
            String name,
            String email,
            String accessToken,
            String refreshToken,
            Instant createdAt
    ) {}

    public record RefreshTokenRequest(
            String refreshToken
    ) {}

    public record RefreshTokenResponse(
            String accessToken,
            String refreshToken
    ) {}

    public record UserProfileResponse(
            Long id,
            String name,
            String email,
            String role,
            String status,
            Instant createdAt,
            Instant updatedAt
    ) {}
}
