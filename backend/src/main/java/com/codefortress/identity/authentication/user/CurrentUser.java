package com.codefortress.identity.user;

import java.util.UUID;

public record CurrentUser(
        UUID id,
        String displayName,
        String email
) {
}