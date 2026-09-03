package com.codefortress.identity.authentication;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class AccessTokenServiceTest {

    @Autowired
    private AccessTokenService accessTokenService;

    @Autowired
    private JwtDecoder jwtDecoder;

    @Test
    void shouldIssueValidAccessToken() {
        UUID userId = UUID.randomUUID();

        AccessToken accessToken = accessTokenService.issue(
                userId,
                "kayque@example.com"
        );

        Jwt decodedToken = jwtDecoder.decode(
                accessToken.value()
        );

        assertThat(decodedToken.getSubject())
                .isEqualTo(userId.toString());

        assertThat(decodedToken.getIssuer().toString())
                .isEqualTo("https://api.codefortress.local");

        assertThat(decodedToken.getClaimAsString("email"))
                .isEqualTo("kayque@example.com");

        assertThat(decodedToken.getClaimAsString("type"))
                .isEqualTo("access");

        assertThat(decodedToken.getExpiresAt())
                .isEqualTo(accessToken.expiresAt());

        assertThat(Duration.between(
                decodedToken.getIssuedAt(),
                decodedToken.getExpiresAt()
        )).isEqualTo(Duration.ofMinutes(15));
    }
}