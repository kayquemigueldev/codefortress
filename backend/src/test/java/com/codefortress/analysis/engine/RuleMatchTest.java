package com.codefortress.analysis.engine;

import com.codefortress.analysis.Severity;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RuleMatchTest {

    @Test
    void shouldCreateValidRuleMatch() {
        RuleMatch match = new RuleMatch(
                "CF-SEC-001",
                Severity.CRITICAL,
                "src/main/java/Config.java",
                12,
                12,
                "apiKey = \"sk_live_********\""
        );

        assertThat(match.ruleKey())
                .isEqualTo("CF-SEC-001");

        assertThat(match.severity())
                .isEqualTo(Severity.CRITICAL);

        assertThat(match.filePath())
                .isEqualTo(
                        "src/main/java/Config.java"
                );

        assertThat(match.startLine())
                .isEqualTo(12);

        assertThat(match.endLine())
                .isEqualTo(12);

        assertThat(match.redactedEvidence())
                .contains("********");
    }

    @Test
    void shouldRejectInvalidLineRange() {
        assertThatThrownBy(() ->
                new RuleMatch(
                        "CF-SEC-001",
                        Severity.CRITICAL,
                        "Config.java",
                        20,
                        10,
                        "redacted"
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
    void shouldRejectZeroStartLine() {
        assertThatThrownBy(() ->
                new RuleMatch(
                        "CF-SEC-001",
                        Severity.CRITICAL,
                        "Config.java",
                        0,
                        1,
                        "redacted"
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "startLine must be greater than zero"
                );
    }

    @Test
    void shouldRejectBlankRuleKey() {
        assertThatThrownBy(() ->
                new RuleMatch(
                        " ",
                        Severity.CRITICAL,
                        "Config.java",
                        1,
                        1,
                        "redacted"
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "ruleKey must not be blank"
                );
    }

    @Test
    void shouldRejectMissingSeverity() {
        assertThatThrownBy(() ->
                new RuleMatch(
                        "CF-SEC-001",
                        null,
                        "Config.java",
                        1,
                        1,
                        "redacted"
                )
        )
                .isInstanceOf(
                        NullPointerException.class
                )
                .hasMessage(
                        "severity must not be null"
                );
    }
}