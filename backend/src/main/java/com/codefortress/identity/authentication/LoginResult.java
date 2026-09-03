package com.codefortress.identity.authentication;

import java.util.UUID;

public record LoginResult(
        UUID userId,
        String displayName,
        String email,
        AccessToken accessToken
) {
}