package com.codefortress.identity.registration;

import com.codefortress.identity.user.User;
import com.codefortress.identity.user.UserRepository;
import com.codefortress.identity.user.UserStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class RegisterUserServiceTest {

    @Autowired
    private RegisterUserService registerUserService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void shouldRegisterUserWithHashedPassword() {
        String rawPassword = "a-strong-password";

        RegisteredUser registeredUser = registerUserService.register(
                new RegisterUserCommand(
                        "Kayque Miguel",
                        "  KAYQUE@EXAMPLE.COM  ",
                        rawPassword
                )
        );

        User persistedUser = userRepository
                .findByEmail("kayque@example.com")
                .orElseThrow();

        assertThat(registeredUser.id()).isNotNull();
        assertThat(registeredUser.email())
                .isEqualTo("kayque@example.com");
        assertThat(registeredUser.displayName())
                .isEqualTo("Kayque Miguel");

        assertThat(persistedUser.getStatus())
                .isEqualTo(UserStatus.ACTIVE);
        assertThat(persistedUser.getPasswordHash())
                .isNotEqualTo(rawPassword);
        assertThat(
                passwordEncoder.matches(
                        rawPassword,
                        persistedUser.getPasswordHash()
                )
        ).isTrue();
    }

    @Test
    void shouldRejectDuplicatedEmailIgnoringCaseAndSpaces() {
        registerUserService.register(
                new RegisterUserCommand(
                        "Kayque Miguel",
                        "kayque@example.com",
                        "first-password"
                )
        );

        assertThatThrownBy(() ->
                registerUserService.register(
                        new RegisterUserCommand(
                                "Outro usuário",
                                "  KAYQUE@EXAMPLE.COM ",
                                "second-password"
                        )
                )
        ).isInstanceOf(EmailAlreadyRegisteredException.class);

        assertThat(userRepository.count()).isEqualTo(1);
    }
}