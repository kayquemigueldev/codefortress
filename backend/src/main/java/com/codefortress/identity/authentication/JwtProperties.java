package com.codefortress.identity.authentication;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "security.jwt")
public record JwtProperties(
        String secret,
        String issuer,
        Duration accessTokenTtl,
        Duration refreshTokenTtl
) {

    public JwtProperties {
        if (secret == null || secret.isBlank()) {
            throw new IllegalArgumentException(
                    "security.jwt.secret must be configured"
            );
        }

        if (issuer == null || issuer.isBlank()) {
            throw new IllegalArgumentException(
                    "security.jwt.issuer must be configured"
            );
        }

        requirePositive(accessTokenTtl, "access-token-ttl");
        requirePositive(refreshTokenTtl, "refresh-token-ttl");
    }

    private static void requirePositive(
            Duration duration,
            String propertyName
    ) {
        if (duration == null
                || duration.isZero()
                || duration.isNegative()) {
            throw new IllegalArgumentException(
                    "security.jwt."
                            + propertyName
                            + " must be positive"
            );
        }
    }
}