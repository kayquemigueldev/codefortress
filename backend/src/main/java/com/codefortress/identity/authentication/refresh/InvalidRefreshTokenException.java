package com.codefortress.identity.authentication.refresh;

public class InvalidRefreshTokenException extends RuntimeException {

    public InvalidRefreshTokenException() {
        super("Invalid or expired refresh token");
    }
}