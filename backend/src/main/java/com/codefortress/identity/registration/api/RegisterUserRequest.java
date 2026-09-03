package com.codefortress.identity.registration.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterUserRequest(

        @NotBlank(message = "displayName must not be blank")
        @Size(
                max = 120,
                message = "displayName must have at most 120 characters"
        )
        String displayName,

        @NotBlank(message = "email must not be blank")
        @Email(message = "email must be valid")
        @Size(
                max = 320,
                message = "email must have at most 320 characters"
        )
        String email,

        @NotBlank(message = "password must not be blank")
        @Size(
                min = 12,
                max = 72,
                message = "password must have between 12 and 72 characters"
        )
        String password
) {

        public RegisterUserRequest {
                displayName = trim(displayName);
                email = trim(email);
        }

        private static String trim(String value) {
                return value == null ? null : value.trim();
        }
}