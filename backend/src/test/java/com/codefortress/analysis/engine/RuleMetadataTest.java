package com.codefortress.analysis.engine;

import com.codefortress.analysis.FindingCategory;
import com.codefortress.analysis.Severity;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RuleMetadataTest {

    @Test
    void shouldCreateValidMetadata() {
        RuleMetadata metadata =
                metadata();

        assertThat(metadata.key())
                .isEqualTo("CF-SEC-001");

        assertThat(metadata.version())
                .isEqualTo("1.0.0");

        assertThat(metadata.title())
                .isEqualTo(
                        "Hardcoded Secret"
                );

        assertThat(metadata.category())
                .isEqualTo(
                        FindingCategory.SECRETS
                );

        assertThat(metadata.defaultSeverity())
                .isEqualTo(
                        Severity.CRITICAL
                );
    }

    @Test
    void shouldRejectBlankKey() {
        assertThatThrownBy(() ->
                new RuleMetadata(
                        " ",
                        "1.0.0",
                        "Hardcoded Secret",
                        FindingCategory.SECRETS,
                        Severity.CRITICAL,
                        "Description",
                        "Impact",
                        "Recommendation"
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "key must not be blank"
                );
    }

    @Test
    void shouldRejectBlankVersion() {
        assertThatThrownBy(() ->
                new RuleMetadata(
                        "CF-SEC-001",
                        "",
                        "Hardcoded Secret",
                        FindingCategory.SECRETS,
                        Severity.CRITICAL,
                        "Description",
                        "Impact",
                        "Recommendation"
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "version must not be blank"
                );
    }

    @Test
    void shouldRejectBlankTitle() {
        assertThatThrownBy(() ->
                new RuleMetadata(
                        "CF-SEC-001",
                        "1.0.0",
                        null,
                        FindingCategory.SECRETS,
                        Severity.CRITICAL,
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
    void shouldRejectMissingCategory() {
        assertThatThrownBy(() ->
                new RuleMetadata(
                        "CF-SEC-001",
                        "1.0.0",
                        "Hardcoded Secret",
                        null,
                        Severity.CRITICAL,
                        "Description",
                        "Impact",
                        "Recommendation"
                )
        )
                .isInstanceOf(
                        NullPointerException.class
                )
                .hasMessage(
                        "category must not be null"
                );
    }

    @Test
    void shouldRejectMissingDefaultSeverity() {
        assertThatThrownBy(() ->
                new RuleMetadata(
                        "CF-SEC-001",
                        "1.0.0",
                        "Hardcoded Secret",
                        FindingCategory.SECRETS,
                        null,
                        "Description",
                        "Impact",
                        "Recommendation"
                )
        )
                .isInstanceOf(
                        NullPointerException.class
                )
                .hasMessage(
                        "defaultSeverity must not be null"
                );
    }

    @Test
    void shouldRejectBlankDescription() {
        assertThatThrownBy(() ->
                new RuleMetadata(
                        "CF-SEC-001",
                        "1.0.0",
                        "Hardcoded Secret",
                        FindingCategory.SECRETS,
                        Severity.CRITICAL,
                        "",
                        "Impact",
                        "Recommendation"
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "description must not be blank"
                );
    }

    @Test
    void shouldRejectBlankImpact() {
        assertThatThrownBy(() ->
                new RuleMetadata(
                        "CF-SEC-001",
                        "1.0.0",
                        "Hardcoded Secret",
                        FindingCategory.SECRETS,
                        Severity.CRITICAL,
                        "Description",
                        " ",
                        "Recommendation"
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "impact must not be blank"
                );
    }

    @Test
    void shouldRejectBlankRecommendation() {
        assertThatThrownBy(() ->
                new RuleMetadata(
                        "CF-SEC-001",
                        "1.0.0",
                        "Hardcoded Secret",
                        FindingCategory.SECRETS,
                        Severity.CRITICAL,
                        "Description",
                        "Impact",
                        null
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "recommendation must not be blank"
                );
    }

    private RuleMetadata metadata() {
        return new RuleMetadata(
                "CF-SEC-001",
                "1.0.0",
                "Hardcoded Secret",
                FindingCategory.SECRETS,
                Severity.CRITICAL,
                "Detects secrets embedded directly in source code.",
                "Exposed credentials may allow unauthorized access.",
                "Move the secret to a secure external configuration."
        );
    }
}