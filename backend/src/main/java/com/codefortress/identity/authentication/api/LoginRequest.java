package com.codefortress.identity.authentication.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(

        @NotBlank(message = "email must not be blank")
        @Email(message = "email must be valid")
        @Size(
                max = 320,
                message = "email must have at most 320 characters"
        )
        String email,

        @NotBlank(message = "password must not be blank")
        @Size(
                max = 72,
                message = "password must have at most 72 characters"
        )
        String password
) {

    public LoginRequest {
        email = email == null ? null : email.trim();
    }
}