package com.codefortress.identity.authentication;

import java.time.Instant;

public record AccessToken(
        String value,
        Instant expiresAt
) {
}