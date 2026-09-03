package com.codefortress.identity.authentication;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

public record LoginCommand(
        String email,
        String password
) {

    public LoginCommand {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException(
                    "email must not be blank"
            );
        }

        email = email.trim().toLowerCase(Locale.ROOT);

        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException(
                    "password must not be blank"
            );
        }

        if (password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException(
                    "password must have at most 72 bytes"
            );
        }
    }
}