package com.codefortress.identity.registration;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

public record RegisterUserCommand(
        String displayName,
        String email,
        String password
) {

    public RegisterUserCommand {
        displayName = requireText(displayName, "displayName");
        email = requireText(email, "email").toLowerCase(Locale.ROOT);

        if (displayName.length() > 120) {
            throw new IllegalArgumentException(
                    "displayName must have at most 120 characters"
            );
        }

        if (email.length() > 320) {
            throw new IllegalArgumentException(
                    "email must have at most 320 characters"
            );
        }

        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException(
                    "password must not be blank"
            );
        }

        int passwordSize = password
                .getBytes(StandardCharsets.UTF_8)
                .length;

        if (passwordSize < 12 || passwordSize > 72) {
            throw new IllegalArgumentException(
                    "password must contain between 12 and 72 bytes"
            );
        }
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    fieldName + " must not be blank"
            );
        }

        return value.trim();
    }
}