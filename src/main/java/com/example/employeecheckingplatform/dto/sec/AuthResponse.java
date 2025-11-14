package com.example.employeecheckingplatform.dto.sec;

public record AuthResponse(
        String accessToken,
        String tokenType,
        long   expiresInSeconds
) {}