package com.codefortress.identity.authentication.refresh;

import com.codefortress.identity.registration.RegisterUserCommand;
import com.codefortress.identity.registration.RegisterUserService;
import com.codefortress.identity.registration.RegisteredUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class RefreshTokenServiceTest {

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private RegisterUserService registerUserService;

    @Test
    void shouldIssueRefreshTokenWithoutStoringRawValue() {
        RegisteredUser user = registerUser();

        IssuedRefreshToken issuedToken =
                refreshTokenService.issue(user.id());

        RefreshToken persistedToken = refreshTokenRepository
                .findAll()
                .getFirst();

        assertThat(issuedToken.value()).isNotBlank();
        assertThat(persistedToken.getTokenHash()).hasSize(64);
        assertThat(persistedToken.getTokenHash())
                .isNotEqualTo(issuedToken.value());
        assertThat(persistedToken.getTokenHash())
                .isEqualTo(hash(issuedToken.value()));
        assertThat(persistedToken.isActiveAt(
                persistedToken.getCreatedAt()
        )).isTrue();
    }

    @Test
    void shouldRotateRefreshToken() {
        RegisteredUser user = registerUser();

        IssuedRefreshToken original =
                refreshTokenService.issue(user.id());

        RotatedRefreshToken rotated =
                refreshTokenService.rotate(original.value());

        RefreshToken originalEntity = refreshTokenRepository
                .findByTokenHashForUpdate(hash(original.value()))
                .orElseThrow();

        RefreshToken replacementEntity = refreshTokenRepository
                .findByTokenHashForUpdate(
                        hash(rotated.refreshToken().value())
                )
                .orElseThrow();

        assertThat(rotated.userId()).isEqualTo(user.id());
        assertThat(rotated.refreshToken().value())
                .isNotEqualTo(original.value());

        assertThat(originalEntity.getRevokedAt()).isNotNull();
        assertThat(originalEntity.getReplacedByTokenId())
                .isEqualTo(replacementEntity.getId());

        assertThat(replacementEntity.getFamilyId())
                .isEqualTo(originalEntity.getFamilyId());
        assertThat(replacementEntity.getRevokedAt()).isNull();
    }

    @Test
    void shouldRevokeFamilyWhenOldTokenIsReused() {
        RegisteredUser user = registerUser();

        IssuedRefreshToken original =
                refreshTokenService.issue(user.id());

        RotatedRefreshToken rotated =
                refreshTokenService.rotate(original.value());

        assertThatThrownBy(() ->
                refreshTokenService.rotate(original.value())
        ).isInstanceOf(InvalidRefreshTokenException.class);

        RefreshToken replacementEntity = refreshTokenRepository
                .findByTokenHashForUpdate(
                        hash(rotated.refreshToken().value())
                )
                .orElseThrow();

        assertThat(replacementEntity.getRevokedAt()).isNotNull();
    }

    private RegisteredUser registerUser() {
        return registerUserService.register(
                new RegisterUserCommand(
                        "Kayque Miguel",
                        "kayque@example.com",
                        "correct-password"
                )
        );
    }

    private String hash(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance(
                    "SHA-256"
            );

            return HexFormat.of().formatHex(
                    digest.digest(
                            value.getBytes(StandardCharsets.UTF_8)
                    )
            );
        } catch (NoSuchAlgorithmException exception) {
            throw new AssertionError(exception);
        }
    }
}