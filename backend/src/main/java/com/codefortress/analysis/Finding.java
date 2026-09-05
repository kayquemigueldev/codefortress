package com.codefortress.analysis;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "findings")
public class Finding {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "analysis_id",
            nullable = false,
            updatable = false
    )
    private Analysis analysis;

    @Column(
            name = "rule_key",
            nullable = false,
            length = 100,
            updatable = false
    )
    private String ruleKey;

    @Column(
            name = "rule_version",
            nullable = false,
            length = 30,
            updatable = false
    )
    private String ruleVersion;

    @Column(
            nullable = false,
            length = 64,
            updatable = false
    )
    private String fingerprint;

    @Column(
            nullable = false,
            length = 200,
            updatable = false
    )
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 30,
            updatable = false
    )
    private FindingCategory category;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 20,
            updatable = false
    )
    private Severity severity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private FindingStatus status;

    @Column(
            name = "file_path",
            nullable = false,
            length = 1000,
            updatable = false
    )
    private String filePath;

    @Column(
            name = "start_line",
            nullable = false,
            updatable = false
    )
    private int startLine;

    @Column(
            name = "end_line",
            nullable = false,
            updatable = false
    )
    private int endLine;

    @Column(
            name = "code_excerpt",
            nullable = false,
            columnDefinition = "TEXT",
            updatable = false
    )
    private String codeExcerpt;

    @Column(
            nullable = false,
            columnDefinition = "TEXT",
            updatable = false
    )
    private String description;

    @Column(
            nullable = false,
            columnDefinition = "TEXT",
            updatable = false
    )
    private String impact;

    @Column(
            nullable = false,
            columnDefinition = "TEXT",
            updatable = false
    )
    private String recommendation;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private Instant createdAt;

    @Column(
            name = "status_updated_at",
            nullable = false
    )
    private Instant statusUpdatedAt;

    protected Finding() {
    }

    private Finding(
            Analysis analysis,
            String ruleKey,
            String ruleVersion,
            String fingerprint,
            String title,
            FindingCategory category,
            Severity severity,
            String filePath,
            int startLine,
            int endLine,
            String codeExcerpt,
            String description,
            String impact,
            String recommendation
    ) {
        this.analysis = Objects.requireNonNull(
                analysis,
                "analysis must not be null"
        );

        this.ruleKey = requireText(
                ruleKey,
                "ruleKey",
                100
        );

        this.ruleVersion = requireText(
                ruleVersion,
                "ruleVersion",
                30
        );

        this.fingerprint =
                validateFingerprint(fingerprint);

        this.title = requireText(
                title,
                "title",
                200
        );

        this.category = Objects.requireNonNull(
                category,
                "category must not be null"
        );

        this.severity = Objects.requireNonNull(
                severity,
                "severity must not be null"
        );

        this.status = FindingStatus.OPEN;

        this.filePath = requireText(
                filePath,
                "filePath",
                1000
        );

        validateLines(
                startLine,
                endLine
        );

        this.startLine = startLine;
        this.endLine = endLine;

        this.codeExcerpt =
                requireContent(
                        codeExcerpt,
                        "codeExcerpt"
                );

        this.description =
                requireContent(
                        description,
                        "description"
                );

        this.impact =
                requireContent(
                        impact,
                        "impact"
                );

        this.recommendation =
                requireContent(
                        recommendation,
                        "recommendation"
                );
    }

    public static Finding create(
            Analysis analysis,
            String ruleKey,
            String ruleVersion,
            String fingerprint,
            String title,
            FindingCategory category,
            Severity severity,
            String filePath,
            int startLine,
            int endLine,
            String codeExcerpt,
            String description,
            String impact,
            String recommendation
    ) {
        return new Finding(
                analysis,
                ruleKey,
                ruleVersion,
                fingerprint,
                title,
                category,
                severity,
                filePath,
                startLine,
                endLine,
                codeExcerpt,
                description,
                impact,
                recommendation
        );
    }

    private static void validateLines(
            int startLine,
            int endLine
    ) {
        if (startLine < 1) {
            throw new IllegalArgumentException(
                    "startLine must be greater than zero"
            );
        }

        if (endLine < startLine) {
            throw new IllegalArgumentException(
                    "endLine must not be before startLine"
            );
        }
    }

    private static String validateFingerprint(
            String fingerprint
    ) {
        String validated =
                requireText(
                        fingerprint,
                        "fingerprint",
                        64
                );

        if (validated.length() != 64
                || !validated.matches(
                "[0-9a-fA-F]{64}"
        )) {
            throw new IllegalArgumentException(
                    "fingerprint must be a 64-character hexadecimal value"
            );
        }

        return validated.toLowerCase(
                Locale.ROOT
        );
    }

    private static String requireText(
            String value,
            String fieldName,
            int maxLength
    ) {
        if (value == null
                || value.isBlank()) {
            throw new IllegalArgumentException(
                    fieldName + " must not be blank"
            );
        }

        String trimmed =
                value.trim();

        if (trimmed.length() > maxLength) {
            throw new IllegalArgumentException(
                    fieldName
                            + " must not exceed "
                            + maxLength
                            + " characters"
            );
        }

        return trimmed;
    }

    private static String requireContent(
            String value,
            String fieldName
    ) {
        if (value == null
                || value.isBlank()) {
            throw new IllegalArgumentException(
                    fieldName + " must not be blank"
            );
        }

        return value;
    }

    @PrePersist
    private void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }

        Instant now =
                Instant.now();

        if (createdAt == null) {
            createdAt = now;
        }

        if (statusUpdatedAt == null) {
            statusUpdatedAt = now;
        }
    }

    public UUID getId() {
        return id;
    }

    public Analysis getAnalysis() {
        return analysis;
    }

    public String getRuleKey() {
        return ruleKey;
    }

    public String getRuleVersion() {
        return ruleVersion;
    }

    public String getFingerprint() {
        return fingerprint;
    }

    public String getTitle() {
        return title;
    }

    public FindingCategory getCategory() {
        return category;
    }

    public Severity getSeverity() {
        return severity;
    }

    public FindingStatus getStatus() {
        return status;
    }

    public String getFilePath() {
        return filePath;
    }

    public int getStartLine() {
        return startLine;
    }

    public int getEndLine() {
        return endLine;
    }

    public String getCodeExcerpt() {
        return codeExcerpt;
    }

    public String getDescription() {
        return description;
    }

    public String getImpact() {
        return impact;
    }

    public String getRecommendation() {
        return recommendation;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getStatusUpdatedAt() {
        return statusUpdatedAt;
    }
}