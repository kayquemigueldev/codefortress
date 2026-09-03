package com.codefortress.identity.authentication.refresh;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(name = "family_id", nullable = false, updatable = false)
    private UUID familyId;

    @Column(
            name = "token_hash",
            nullable = false,
            unique = true,
            length = 64,
            updatable = false
    )
    private String tokenHash;

    @Column(name = "expires_at", nullable = false, updatable = false)
    private Instant expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Column(name = "replaced_by_token_id")
    private UUID replacedByTokenId;

    protected RefreshToken() {
    }

    private RefreshToken(
            UUID userId,
            UUID familyId,
            String tokenHash,
            Instant createdAt,
            Instant expiresAt
    ) {
        this.id = UUID.randomUUID();
        this.userId = Objects.requireNonNull(userId);
        this.familyId = Objects.requireNonNull(familyId);
        this.tokenHash = requireValidHash(tokenHash);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.expiresAt = Objects.requireNonNull(expiresAt);

        if (!expiresAt.isAfter(createdAt)) {
            throw new IllegalArgumentException(
                    "expiresAt must be after createdAt"
            );
        }
    }

    public static RefreshToken issue(
            UUID userId,
            UUID familyId,
            String tokenHash,
            Instant createdAt,
            Instant expiresAt
    ) {
        return new RefreshToken(
                userId,
                familyId,
                tokenHash,
                createdAt,
                expiresAt
        );
    }

    public boolean isActiveAt(Instant instant) {
        return revokedAt == null && expiresAt.isAfter(instant);
    }

    public void revoke(
            Instant revokedAt,
            UUID replacedByTokenId
    ) {
        if (this.revokedAt != null) {
            return;
        }

        this.revokedAt = Objects.requireNonNull(revokedAt);
        this.replacedByTokenId = replacedByTokenId;
    }

    private static String requireValidHash(String tokenHash) {
        if (tokenHash == null || tokenHash.length() != 64) {
            throw new IllegalArgumentException(
                    "tokenHash must contain 64 characters"
            );
        }

        return tokenHash;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getFamilyId() {
        return familyId;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getRevokedAt() {
        return revokedAt;
    }

    public UUID getReplacedByTokenId() {
        return replacedByTokenId;
    }
}