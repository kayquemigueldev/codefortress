package com.codefortress.identity.authentication.refresh;

import java.util.UUID;

public record RotatedRefreshToken(
        UUID userId,
        IssuedRefreshToken refreshToken
) {
}