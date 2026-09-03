package com.codefortress.identity.authentication.api;

import com.codefortress.identity.authentication.LoginCommand;
import com.codefortress.identity.authentication.LoginResult;
import com.codefortress.identity.authentication.LoginService;
import com.codefortress.identity.authentication.RefreshSessionService;
import com.codefortress.identity.authentication.refresh.InvalidRefreshTokenException;
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
    private final RefreshTokenCookieFactory refreshTokenCookieFactory;

    public AuthenticationController(
            LoginService loginService,
            RefreshSessionService refreshSessionService,
            RefreshTokenCookieFactory refreshTokenCookieFactory
    ) {
        this.loginService = loginService;
        this.refreshSessionService = refreshSessionService;
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
        String rawRefreshToken = readRefreshToken(request);

        LoginResult result = refreshSessionService.refresh(
                rawRefreshToken
        );

        return responseWithRefreshCookie(result);
    }

    private String readRefreshToken(HttpServletRequest request) {
        Cookie refreshCookie = WebUtils.getCookie(
                request,
                refreshTokenCookieFactory.cookieName()
        );

        if (refreshCookie == null
                || refreshCookie.getValue() == null
                || refreshCookie.getValue().isBlank()) {
            throw new InvalidRefreshTokenException();
        }

        return refreshCookie.getValue();
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