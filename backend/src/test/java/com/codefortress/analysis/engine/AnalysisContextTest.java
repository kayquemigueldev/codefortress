package com.codefortress.analysis.engine;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AnalysisContextTest {

    @Test
    void shouldCreateValidAnalysisContext() {
        UUID analysisId = UUID.randomUUID();

        AnalysisContext context =
                new AnalysisContext(
                        analysisId,
                        "ruleset-java-v1"
                );

        assertThat(context.analysisId())
                .isEqualTo(analysisId);

        assertThat(context.ruleSetVersion())
                .isEqualTo("ruleset-java-v1");
    }

    @Test
    void shouldRejectBlankRuleSetVersion() {
        assertThatThrownBy(() ->
                new AnalysisContext(
                        UUID.randomUUID(),
                        " "
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "ruleSetVersion must not be blank"
                );
    }

    @Test
    void shouldRejectMissingAnalysisId() {
        assertThatThrownBy(() ->
                new AnalysisContext(
                        null,
                        "ruleset-java-v1"
                )
        )
                .isInstanceOf(
                        NullPointerException.class
                )
                .hasMessage(
                        "analysisId must not be null"
                );
    }
}