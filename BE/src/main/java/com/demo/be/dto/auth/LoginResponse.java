package com.demo.be.dto.auth;

public record LoginResponse(
        String accessToken,
        String tokenType,
        String username,
        String fullName,
        String role
) {
}
