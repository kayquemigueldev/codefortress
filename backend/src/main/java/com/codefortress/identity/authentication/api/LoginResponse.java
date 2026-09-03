package com.codefortress.identity.authentication.api;

import com.codefortress.identity.authentication.LoginResult;

import java.time.Instant;
import java.util.UUID;

public record LoginResponse(
        String accessToken,
        String tokenType,
        Instant expiresAt,
        UserResponse user
) {

    public static LoginResponse from(LoginResult result) {
        return new LoginResponse(
                result.accessToken().value(),
                "Bearer",
                result.accessToken().expiresAt(),
                new UserResponse(
                        result.userId(),
                        result.displayName(),
                        result.email()
                )
        );
    }

    public record UserResponse(
            UUID id,
            String displayName,
            String email
    ) {
    }
}