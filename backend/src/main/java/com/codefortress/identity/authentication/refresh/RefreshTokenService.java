package com.codefortress.identity.authentication.refresh;

import com.codefortress.identity.authentication.JwtProperties;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private static final int TOKEN_SIZE_BYTES = 32;

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProperties jwtProperties;
    private final SecureRandom secureRandom = new SecureRandom();

    public RefreshTokenService(
            RefreshTokenRepository refreshTokenRepository,
            JwtProperties jwtProperties
    ) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtProperties = jwtProperties;
    }

    @Transactional
    public IssuedRefreshToken issue(UUID userId) {
        Instant issuedAt = now();

        GeneratedRefreshToken generated = issueInFamily(
                userId,
                UUID.randomUUID(),
                issuedAt
        );

        return generated.result();
    }

    @Transactional(
            noRollbackFor = InvalidRefreshTokenException.class
    )
    public RotatedRefreshToken rotate(String rawToken) {
        String tokenHash = hashToken(rawToken);

        RefreshToken currentToken = refreshTokenRepository
                .findByTokenHashForUpdate(tokenHash)
                .orElseThrow(InvalidRefreshTokenException::new);

        Instant rotatedAt = now();

        if (currentToken.getRevokedAt() != null) {
            revokeFamily(
                    currentToken.getFamilyId(),
                    rotatedAt
            );

            throw new InvalidRefreshTokenException();
        }

        if (!currentToken.isActiveAt(rotatedAt)) {
            currentToken.revoke(rotatedAt, null);
            throw new InvalidRefreshTokenException();
        }

        GeneratedRefreshToken replacement = issueInFamily(
                currentToken.getUserId(),
                currentToken.getFamilyId(),
                rotatedAt
        );

        currentToken.revoke(
                rotatedAt,
                replacement.entity().getId()
        );

        return new RotatedRefreshToken(
                currentToken.getUserId(),
                replacement.result()
        );
    }

    @Transactional
    public void revoke(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            return;
        }

        String tokenHash = hashToken(rawToken);

        refreshTokenRepository
                .findByTokenHashForUpdate(tokenHash)
                .ifPresent(token -> token.revoke(now(), null));
    }

    private GeneratedRefreshToken issueInFamily(
            UUID userId,
            UUID familyId,
            Instant issuedAt
    ) {
        String rawToken = generateToken();
        String tokenHash = hashToken(rawToken);

        Instant expiresAt = issuedAt.plus(
                jwtProperties.refreshTokenTtl()
        );

        RefreshToken entity = RefreshToken.issue(
                userId,
                familyId,
                tokenHash,
                issuedAt,
                expiresAt
        );

        refreshTokenRepository.saveAndFlush(entity);

        return new GeneratedRefreshToken(
                entity,
                new IssuedRefreshToken(
                        rawToken,
                        expiresAt
                )
        );
    }

    private void revokeFamily(
            UUID familyId,
            Instant revokedAt
    ) {
        List<RefreshToken> activeTokens =
                refreshTokenRepository
                        .findAllByFamilyIdAndRevokedAtIsNull(
                                familyId
                        );

        activeTokens.forEach(token ->
                token.revoke(revokedAt, null)
        );

        refreshTokenRepository.saveAll(activeTokens);
    }

    private String generateToken() {
        byte[] randomBytes = new byte[TOKEN_SIZE_BYTES];
        secureRandom.nextBytes(randomBytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);
    }

    private String hashToken(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new InvalidRefreshTokenException();
        }

        try {
            MessageDigest digest = MessageDigest.getInstance(
                    "SHA-256"
            );

            byte[] hash = digest.digest(
                    rawToken.getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "SHA-256 is not available",
                    exception
            );
        }
    }

    private Instant now() {
        return Instant.now().truncatedTo(ChronoUnit.SECONDS);
    }

    private record GeneratedRefreshToken(
            RefreshToken entity,
            IssuedRefreshToken result
    ) {
    }
}