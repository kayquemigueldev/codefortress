package com.codefortress.identity.authentication.api;

import com.codefortress.identity.authentication.LoginCommand;
import com.codefortress.identity.authentication.LoginResult;
import com.codefortress.identity.authentication.LoginService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthenticationController {

    private final LoginService loginService;
    private final RefreshTokenCookieFactory refreshTokenCookieFactory;

    public AuthenticationController(
            LoginService loginService,
            RefreshTokenCookieFactory refreshTokenCookieFactory
    ) {
        this.loginService = loginService;
        this.refreshTokenCookieFactory =
                refreshTokenCookieFactory;
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