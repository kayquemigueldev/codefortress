package com.codefortress.identity.authentication.api;

import com.codefortress.identity.authentication.JwtProperties;
import com.codefortress.identity.authentication.refresh.IssuedRefreshToken;
import com.codefortress.identity.authentication.refresh.RefreshCookieProperties;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class RefreshTokenCookieFactory {

    private final RefreshCookieProperties cookieProperties;
    private final JwtProperties jwtProperties;

    public RefreshTokenCookieFactory(
            RefreshCookieProperties cookieProperties,
            JwtProperties jwtProperties
    ) {
        this.cookieProperties = cookieProperties;
        this.jwtProperties = jwtProperties;
    }

    public ResponseCookie create(IssuedRefreshToken refreshToken) {
        return ResponseCookie
                .from(
                        cookieProperties.name(),
                        refreshToken.value()
                )
                .httpOnly(true)
                .secure(cookieProperties.secure())
                .sameSite(cookieProperties.sameSite())
                .path(cookieProperties.path())
                .maxAge(jwtProperties.refreshTokenTtl())
                .build();
    }

    public ResponseCookie clear() {
        return ResponseCookie
                .from(cookieProperties.name(), "")
                .httpOnly(true)
                .secure(cookieProperties.secure())
                .sameSite(cookieProperties.sameSite())
                .path(cookieProperties.path())
                .maxAge(Duration.ZERO)
                .build();
    }

    public String cookieName() {
        return cookieProperties.name();
    }
}