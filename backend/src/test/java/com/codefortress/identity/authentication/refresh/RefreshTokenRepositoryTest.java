package com.codefortress.identity.authentication.refresh;

import com.codefortress.identity.registration.RegisterUserCommand;
import com.codefortress.identity.registration.RegisterUserService;
import com.codefortress.identity.registration.RegisteredUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class RefreshTokenRepositoryTest {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private RegisterUserService registerUserService;

    @Test
    void shouldPersistAndFindRefreshTokenByHash() {
        RegisteredUser user = registerUserService.register(
                new RegisterUserCommand(
                        "Kayque Miguel",
                        "kayque@example.com",
                        "correct-password"
                )
        );

        Instant createdAt = Instant.parse(
                "2026-09-03T12:00:00Z"
        );

        RefreshToken token = RefreshToken.issue(
                user.id(),
                UUID.randomUUID(),
                "a".repeat(64),
                createdAt,
                createdAt.plus(30, ChronoUnit.DAYS)
        );

        refreshTokenRepository.saveAndFlush(token);

        RefreshToken persistedToken = refreshTokenRepository
                .findByTokenHashForUpdate("a".repeat(64))
                .orElseThrow();

        assertThat(persistedToken.getId()).isNotNull();
        assertThat(persistedToken.getUserId())
                .isEqualTo(user.id());
        assertThat(persistedToken.getFamilyId()).isNotNull();
        assertThat(persistedToken.getTokenHash())
                .isEqualTo("a".repeat(64));
        assertThat(persistedToken.isActiveAt(
                createdAt.plus(1, ChronoUnit.DAYS)
        )).isTrue();
        assertThat(persistedToken.getRevokedAt()).isNull();
        assertThat(persistedToken.getReplacedByTokenId()).isNull();
    }
}