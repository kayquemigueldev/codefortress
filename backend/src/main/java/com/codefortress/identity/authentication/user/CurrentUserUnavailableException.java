package com.codefortress.identity.user;

public class CurrentUserUnavailableException extends RuntimeException {

    public CurrentUserUnavailableException() {
        super("Authenticated user is unavailable");
    }
}