package com.codefortress.analysis.scoring;

import com.codefortress.analysis.FindingCategory;
import com.codefortress.analysis.Severity;
import com.codefortress.analysis.engine.EvaluatedRuleMatch;
import com.codefortress.analysis.engine.RuleMatch;
import com.codefortress.analysis.engine.RuleMetadata;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SecurityScoreCalculatorTest {

    private final SecurityScoreCalculator calculator =
            new SecurityScoreCalculator();

    @Test
    void shouldReturnPerfectScoreWithoutFindings() {
        assertThat(
                calculator.calculate(
                        List.of()
                )
        ).isEqualTo(100);
    }

    @Test
    void shouldReduceScoreForLowFinding() {
        assertThat(
                calculator.calculate(
                        List.of(
                                evaluatedMatch(
                                        Severity.LOW
                                )
                        )
                )
        ).isEqualTo(99);
    }

    @Test
    void shouldCapScoreForMediumFinding() {
        assertThat(
                calculator.calculate(
                        List.of(
                                evaluatedMatch(
                                        Severity.MEDIUM
                                )
                        )
                )
        ).isEqualTo(89);
    }

    @Test
    void shouldCapScoreForHighFinding() {
        assertThat(
                calculator.calculate(
                        List.of(
                                evaluatedMatch(
                                        Severity.HIGH
                                )
                        )
                )
        ).isEqualTo(74);
    }

    @Test
    void shouldCapScoreForCriticalFinding() {
        assertThat(
                calculator.calculate(
                        List.of(
                                evaluatedMatch(
                                        Severity.CRITICAL
                                )
                        )
                )
        ).isEqualTo(49);
    }

    @Test
    void shouldUseHighestSeverityCap() {
        assertThat(
                calculator.calculate(
                        List.of(
                                evaluatedMatch(
                                        Severity.MEDIUM
                                ),
                                evaluatedMatch(
                                        Severity.HIGH
                                ),
                                evaluatedMatch(
                                        Severity.CRITICAL
                                )
                        )
                )
        ).isEqualTo(49);
    }

    @Test
    void shouldIncreasePenaltyForRepeatedFindings() {
        int oneLow =
                calculator.calculate(
                        List.of(
                                evaluatedMatch(
                                        Severity.LOW
                                )
                        )
                );

        int fourLow =
                calculator.calculate(
                        List.of(
                                evaluatedMatch(
                                        Severity.LOW
                                ),
                                evaluatedMatch(
                                        Severity.LOW
                                ),
                                evaluatedMatch(
                                        Severity.LOW
                                ),
                                evaluatedMatch(
                                        Severity.LOW
                                )
                        )
                );

        assertThat(fourLow)
                .isLessThan(oneLow);
    }

    @Test
    void shouldRejectNullMatches() {
        assertThatThrownBy(() ->
                calculator.calculate(null)
        )
                .isInstanceOf(
                        NullPointerException.class
                )
                .hasMessage(
                        "matches must not be null"
                );
    }

    @Test
    void shouldRejectNullMatchEntry() {
        assertThatThrownBy(() ->
                calculator.calculate(
                        java.util.Arrays.asList(
                                (EvaluatedRuleMatch) null
                        )
                )
        )
                .isInstanceOf(
                        NullPointerException.class
                )
                .hasMessage(
                        "evaluatedMatch must not be null"
                );
    }

    private EvaluatedRuleMatch evaluatedMatch(
            Severity severity
    ) {
        RuleMetadata metadata =
                new RuleMetadata(
                        "CF-TEST-001",
                        "1.0.0",
                        "Test Rule",
                        FindingCategory.CODE,
                        severity,
                        "Test description",
                        "Test impact",
                        "Test recommendation"
                );

        RuleMatch match =
                new RuleMatch(
                        "CF-TEST-001",
                        severity,
                        "src/Test.java",
                        1,
                        1,
                        "redacted evidence"
                );

        return new EvaluatedRuleMatch(
                metadata,
                match
        );
    }
}