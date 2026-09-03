package com.codefortress.identity.user.api;

import com.codefortress.identity.user.CurrentUser;

import java.util.UUID;

public record CurrentUserResponse(
        UUID id,
        String displayName,
        String email
) {

    public static CurrentUserResponse from(CurrentUser user) {
        return new CurrentUserResponse(
                user.id(),
                user.displayName(),
                user.email()
        );
    }
}