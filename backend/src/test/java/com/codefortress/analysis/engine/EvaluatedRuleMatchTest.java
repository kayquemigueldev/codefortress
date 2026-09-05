package com.codefortress.analysis.engine;

import com.codefortress.analysis.FindingCategory;
import com.codefortress.analysis.Severity;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EvaluatedRuleMatchTest {

    @Test
    void shouldCreateEvaluatedRuleMatch() {
        RuleMetadata metadata =
                metadata();

        RuleMatch match =
                match(
                        "CF-SEC-001",
                        Severity.CRITICAL
                );

        EvaluatedRuleMatch evaluated =
                new EvaluatedRuleMatch(
                        metadata,
                        match
                );

        assertThat(evaluated.metadata())
                .isSameAs(metadata);

        assertThat(evaluated.match())
                .isSameAs(match);
    }

    @Test
    void shouldAllowSeverityDifferentFromDefaultSeverity() {
        RuleMetadata metadata =
                metadata();

        RuleMatch match =
                match(
                        "CF-SEC-001",
                        Severity.HIGH
                );

        EvaluatedRuleMatch evaluated =
                new EvaluatedRuleMatch(
                        metadata,
                        match
                );

        assertThat(
                evaluated.metadata()
                        .defaultSeverity()
        ).isEqualTo(
                Severity.CRITICAL
        );

        assertThat(
                evaluated.match()
                        .severity()
        ).isEqualTo(
                Severity.HIGH
        );
    }

    @Test
    void shouldRejectMissingMetadata() {
        assertThatThrownBy(() ->
                new EvaluatedRuleMatch(
                        null,
                        match(
                                "CF-SEC-001",
                                Severity.CRITICAL
                        )
                )
        )
                .isInstanceOf(
                        NullPointerException.class
                )
                .hasMessage(
                        "metadata must not be null"
                );
    }

    @Test
    void shouldRejectMissingMatch() {
        assertThatThrownBy(() ->
                new EvaluatedRuleMatch(
                        metadata(),
                        null
                )
        )
                .isInstanceOf(
                        NullPointerException.class
                )
                .hasMessage(
                        "match must not be null"
                );
    }

    @Test
    void shouldRejectDifferentRuleKeys() {
        assertThatThrownBy(() ->
                new EvaluatedRuleMatch(
                        metadata(),
                        match(
                                "CF-CODE-002",
                                Severity.MEDIUM
                        )
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "metadata key must match rule match key"
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
                "Move the secret to secure external configuration."
        );
    }

    private RuleMatch match(
            String ruleKey,
            Severity severity
    ) {
        return new RuleMatch(
                ruleKey,
                severity,
                "Config.java",
                2,
                2,
                "password=********"
        );
    }
}