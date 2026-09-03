package com.codefortress.identity.registration.api;

import com.codefortress.identity.registration.RegisteredUser;

import java.util.UUID;

public record RegisterUserResponse(
        UUID id,
        String displayName,
        String email
) {

    public static RegisterUserResponse from(RegisteredUser user) {
        return new RegisterUserResponse(
                user.id(),
                user.displayName(),
                user.email()
        );
    }
}