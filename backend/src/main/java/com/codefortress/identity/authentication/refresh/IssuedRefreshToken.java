package com.codefortress.identity.authentication.refresh;

import java.time.Instant;

public record IssuedRefreshToken(
        String value,
        Instant expiresAt
) {
}