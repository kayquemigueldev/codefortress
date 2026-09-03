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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class LoginServiceTest {

    @Autowired
    private RegisterUserService registerUserService;

    @Autowired
    private LoginService loginService;

    @Autowired
    private JwtDecoder jwtDecoder;

    @Test
    void shouldAuthenticateUserAndIssueAccessToken() {
        registerUserService.register(
                new RegisterUserCommand(
                        "Kayque Miguel",
                        "kayque@example.com",
                        "correct-password"
                )
        );

        LoginResult result = loginService.login(
                new LoginCommand(
                        "  KAYQUE@EXAMPLE.COM ",
                        "correct-password"
                )
        );

        Jwt decodedToken = jwtDecoder.decode(
                result.accessToken().value()
        );

        assertThat(result.userId()).isNotNull();
        assertThat(result.displayName())
                .isEqualTo("Kayque Miguel");
        assertThat(result.email())
                .isEqualTo("kayque@example.com");

        assertThat(decodedToken.getSubject())
                .isEqualTo(result.userId().toString());
        assertThat(decodedToken.getClaimAsString("email"))
                .isEqualTo("kayque@example.com");
        assertThat(decodedToken.getClaimAsString("type"))
                .isEqualTo("access");
    }

    @Test
    void shouldRejectIncorrectPassword() {
        registerUserService.register(
                new RegisterUserCommand(
                        "Kayque Miguel",
                        "kayque@example.com",
                        "correct-password"
                )
        );

        assertThatThrownBy(() ->
                loginService.login(
                        new LoginCommand(
                                "kayque@example.com",
                                "incorrect-password"
                        )
                )
        )
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid email or password");
    }

    @Test
    void shouldNotRevealWhenEmailDoesNotExist() {
        assertThatThrownBy(() ->
                loginService.login(
                        new LoginCommand(
                                "unknown@example.com",
                                "incorrect-password"
                        )
                )
        )
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid email or password");
    }
}