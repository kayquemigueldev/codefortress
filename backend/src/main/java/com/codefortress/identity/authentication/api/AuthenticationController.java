package com.codefortress.identity.authentication.api;

import com.codefortress.identity.authentication.LoginCommand;
import com.codefortress.identity.authentication.LoginResult;
import com.codefortress.identity.authentication.LoginService;
import com.codefortress.identity.authentication.RefreshSessionService;
import com.codefortress.identity.authentication.refresh.InvalidRefreshTokenException;
import com.codefortress.identity.authentication.refresh.RefreshTokenService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.WebUtils;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthenticationController {

    private final LoginService loginService;
    private final RefreshSessionService refreshSessionService;
    private final RefreshTokenService refreshTokenService;
    private final RefreshTokenCookieFactory refreshTokenCookieFactory;

    public AuthenticationController(
            LoginService loginService,
            RefreshSessionService refreshSessionService,
            RefreshTokenService refreshTokenService,
            RefreshTokenCookieFactory refreshTokenCookieFactory
    ) {
        this.loginService = loginService;
        this.refreshSessionService = refreshSessionService;
        this.refreshTokenService = refreshTokenService;
        this.refreshTokenCookieFactory = refreshTokenCookieFactory;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        LoginResult result = loginService.login(
                new LoginCommand(
                        request.email(),
                        request.password()
                )
        );

        return responseWithRefreshCookie(result);
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(
            HttpServletRequest request
    ) {
        LoginResult result = refreshSessionService.refresh(
                readRefreshToken(request)
        );

        return responseWithRefreshCookie(result);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            HttpServletRequest request
    ) {
        revokeRefreshTokenIfPresent(request);

        ResponseCookie clearedCookie =
                refreshTokenCookieFactory.clear();

        return ResponseEntity
                .noContent()
                .header(
                        HttpHeaders.SET_COOKIE,
                        clearedCookie.toString()
                )
                .build();
    }

    private String readRefreshToken(HttpServletRequest request) {
        Cookie refreshCookie = findRefreshCookie(request);

        if (refreshCookie == null
                || refreshCookie.getValue() == null
                || refreshCookie.getValue().isBlank()) {
            throw new InvalidRefreshTokenException();
        }

        return refreshCookie.getValue();
    }

    private void revokeRefreshTokenIfPresent(
            HttpServletRequest request
    ) {
        Cookie refreshCookie = findRefreshCookie(request);

        if (refreshCookie == null
                || refreshCookie.getValue() == null
                || refreshCookie.getValue().isBlank()) {
            return;
        }

        try {
            refreshTokenService.revoke(
                    refreshCookie.getValue()
            );
        } catch (InvalidRefreshTokenException ignored) {
        }
    }

    private Cookie findRefreshCookie(
            HttpServletRequest request
    ) {
        return WebUtils.getCookie(
                request,
                refreshTokenCookieFactory.cookieName()
        );
    }

    private ResponseEntity<LoginResponse> responseWithRefreshCookie(
            LoginResult result
    ) {
        ResponseCookie refreshCookie =
                refreshTokenCookieFactory.create(
                        result.refreshToken()
                );

        return ResponseEntity
                .ok()
                .header(
                        HttpHeaders.SET_COOKIE,
                        refreshCookie.toString()
                )
                .body(LoginResponse.from(result));
    }
}