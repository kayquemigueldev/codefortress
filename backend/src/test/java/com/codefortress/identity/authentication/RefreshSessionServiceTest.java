package com.codefortress.identity.authentication;

import com.codefortress.identity.registration.RegisterUserCommand;
import com.codefortress.identity.registration.RegisterUserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class RefreshSessionServiceTest {

    @Autowired
    private RegisterUserService registerUserService;

    @Autowired
    private LoginService loginService;

    @Autowired
    private RefreshSessionService refreshSessionService;

    @Autowired
    private JwtDecoder jwtDecoder;

    @Test
    void shouldRotateRefreshTokenAndIssueNewAccessToken() {
        registerUserService.register(
                new RegisterUserCommand(
                        "Kayque Miguel",
                        "kayque@example.com",
                        "correct-password"
                )
        );

        LoginResult login = loginService.login(
                new LoginCommand(
                        "kayque@example.com",
                        "correct-password"
                )
        );

        LoginResult refreshed = refreshSessionService.refresh(
                login.refreshToken().value()
        );

        Jwt decodedAccessToken = jwtDecoder.decode(
                refreshed.accessToken().value()
        );

        assertThat(refreshed.userId()).isEqualTo(login.userId());
        assertThat(refreshed.email()).isEqualTo(login.email());

        assertThat(refreshed.refreshToken().value())
                .isNotEqualTo(login.refreshToken().value());

        assertThat(decodedAccessToken.getSubject())
                .isEqualTo(login.userId().toString());

        assertThat(decodedAccessToken.getClaimAsString("type"))
                .isEqualTo("access");
    }
}