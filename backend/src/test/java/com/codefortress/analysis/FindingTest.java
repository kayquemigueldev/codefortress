package com.codefortress.analysis;

import com.codefortress.identity.user.User;
import com.codefortress.project.Project;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FindingTest {

    @Test
    void shouldCreateOpenFinding() {
        Finding finding =
                finding();

        assertThat(finding.getAnalysis())
                .isNotNull();

        assertThat(finding.getRuleKey())
                .isEqualTo("CF-SEC-001");

        assertThat(finding.getRuleVersion())
                .isEqualTo("1.0.0");

        assertThat(finding.getTitle())
                .isEqualTo(
                        "Hardcoded Secret"
                );

        assertThat(finding.getCategory())
                .isEqualTo(
                        FindingCategory.SECRETS
                );

        assertThat(finding.getSeverity())
                .isEqualTo(
                        Severity.CRITICAL
                );

        assertThat(finding.getStatus())
                .isEqualTo(
                        FindingStatus.OPEN
                );

        assertThat(finding.getFilePath())
                .isEqualTo(
                        "src/main/java/Config.java"
                );

        assertThat(finding.getStartLine())
                .isEqualTo(2);

        assertThat(finding.getEndLine())
                .isEqualTo(2);
    }

    @Test
    void shouldNormalizeFingerprintToLowercase() {
        Finding finding =
                Finding.create(
                        analysis(),
                        "CF-SEC-001",
                        "1.0.0",
                        "A".repeat(64),
                        "Hardcoded Secret",
                        FindingCategory.SECRETS,
                        Severity.CRITICAL,
                        "Config.java",
                        1,
                        1,
                        "String password = \"********\";",
                        "Description",
                        "Impact",
                        "Recommendation"
                );

        assertThat(finding.getFingerprint())
                .isEqualTo(
                        "a".repeat(64)
                );
    }

    @Test
    void shouldRejectInvalidFingerprint() {
        assertThatThrownBy(() ->
                Finding.create(
                        analysis(),
                        "CF-SEC-001",
                        "1.0.0",
                        "invalid",
                        "Hardcoded Secret",
                        FindingCategory.SECRETS,
                        Severity.CRITICAL,
                        "Config.java",
                        1,
                        1,
                        "password=********",
                        "Description",
                        "Impact",
                        "Recommendation"
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "fingerprint must be a 64-character hexadecimal value"
                );
    }

    @Test
    void shouldRejectInvalidLineRange() {
        assertThatThrownBy(() ->
                Finding.create(
                        analysis(),
                        "CF-SEC-001",
                        "1.0.0",
                        "a".repeat(64),
                        "Hardcoded Secret",
                        FindingCategory.SECRETS,
                        Severity.CRITICAL,
                        "Config.java",
                        5,
                        4,
                        "password=********",
                        "Description",
                        "Impact",
                        "Recommendation"
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "endLine must not be before startLine"
                );
    }

    @Test
    void shouldRejectBlankTitle() {
        assertThatThrownBy(() ->
                Finding.create(
                        analysis(),
                        "CF-SEC-001",
                        "1.0.0",
                        "a".repeat(64),
                        " ",
                        FindingCategory.SECRETS,
                        Severity.CRITICAL,
                        "Config.java",
                        1,
                        1,
                        "password=********",
                        "Description",
                        "Impact",
                        "Recommendation"
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "title must not be blank"
                );
    }

    @Test
    void shouldRejectMissingSeverity() {
        assertThatThrownBy(() ->
                Finding.create(
                        analysis(),
                        "CF-SEC-001",
                        "1.0.0",
                        "a".repeat(64),
                        "Hardcoded Secret",
                        FindingCategory.SECRETS,
                        null,
                        "Config.java",
                        1,
                        1,
                        "password=********",
                        "Description",
                        "Impact",
                        "Recommendation"
                )
        )
                .isInstanceOf(
                        NullPointerException.class
                )
                .hasMessage(
                        "severity must not be null"
                );
    }

    private Finding finding() {
        return Finding.create(
                analysis(),
                "CF-SEC-001",
                "1.0.0",
                "a".repeat(64),
                "Hardcoded Secret",
                FindingCategory.SECRETS,
                Severity.CRITICAL,
                "src/main/java/Config.java",
                2,
                2,
                "String password = \"********\";",
                "Detects secrets embedded directly in source code.",
                "Exposed credentials may allow unauthorized access.",
                "Move the secret to secure external configuration."
        );
    }

    private Analysis analysis() {
        User owner =
                User.create(
                        "owner@example.com",
                        "test-password-hash",
                        "Project Owner"
                );

        Project project =
                Project.create(
                        owner,
                        "CodeFortress API",
                        "Project used during automated tests"
                );

        return Analysis.queueUpload(
                project,
                1,
                "source-archive-reference",
                "source.zip",
                "ruleset-java-v1",
                "score-v1"
        );
    }
}