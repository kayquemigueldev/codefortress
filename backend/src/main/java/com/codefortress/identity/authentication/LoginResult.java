package com.codefortress.identity.authentication;

import com.codefortress.identity.authentication.refresh.IssuedRefreshToken;

import java.util.UUID;

public record LoginResult(
        UUID userId,
        String displayName,
        String email,
        AccessToken accessToken,
        IssuedRefreshToken refreshToken
) {
}