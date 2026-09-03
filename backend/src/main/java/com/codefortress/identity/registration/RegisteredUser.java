package com.codefortress.identity.registration;

import java.util.UUID;

public record RegisteredUser(
        UUID id,
        String displayName,
        String email
) {
}