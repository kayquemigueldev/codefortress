package com.codefortress.analysis.engine;

import com.codefortress.analysis.Analysis;
import com.codefortress.analysis.Finding;
import com.codefortress.analysis.FindingCategory;
import com.codefortress.analysis.FindingStatus;
import com.codefortress.analysis.Severity;
import com.codefortress.identity.user.User;
import com.codefortress.project.Project;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FindingFactoryTest {

    private final FindingFactory factory =
            new FindingFactory(
                    new FindingFingerprintGenerator()
            );

    @Test
    void shouldCreateFindingFromEvaluatedRuleMatch() {
        Analysis analysis =
                analysis();

        EvaluatedRuleMatch evaluatedMatch =
                evaluatedMatch(
                        Severity.CRITICAL
                );

        Finding finding =
                factory.create(
                        analysis,
                        evaluatedMatch
                );

        assertThat(finding.getAnalysis())
                .isSameAs(analysis);

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

        assertThat(finding.getCodeExcerpt())
                .isEqualTo(
                        "String password = \"********\";"
                );

        assertThat(finding.getDescription())
                .isEqualTo(
                        "Detects secrets embedded directly in source code."
                );

        assertThat(finding.getImpact())
                .isEqualTo(
                        "Exposed credentials may allow unauthorized access."
                );

        assertThat(finding.getRecommendation())
                .isEqualTo(
                        "Move the secret to secure external configuration."
                );

        assertThat(finding.getFingerprint())
                .hasSize(64)
                .matches("[0-9a-f]{64}");
    }

    @Test
    void shouldUseMatchSeverityInsteadOfDefaultSeverity() {
        Finding finding =
                factory.create(
                        analysis(),
                        evaluatedMatch(
                                Severity.HIGH
                        )
                );

        assertThat(
                finding.getSeverity()
        ).isEqualTo(
                Severity.HIGH
        );
    }

    @Test
    void shouldGenerateSameFingerprintForSameFinding() {
        Finding first =
                factory.create(
                        analysis(),
                        evaluatedMatch(
                                Severity.CRITICAL
                        )
                );

        Finding second =
                factory.create(
                        analysis(),
                        evaluatedMatch(
                                Severity.CRITICAL
                        )
                );

        assertThat(
                first.getFingerprint()
        ).isEqualTo(
                second.getFingerprint()
        );
    }

    @Test
    void shouldRejectMissingAnalysis() {
        assertThatThrownBy(() ->
                factory.create(
                        null,
                        evaluatedMatch(
                                Severity.CRITICAL
                        )
                )
        )
                .isInstanceOf(
                        NullPointerException.class
                )
                .hasMessage(
                        "analysis must not be null"
                );
    }

    @Test
    void shouldRejectMissingEvaluatedMatch() {
        assertThatThrownBy(() ->
                factory.create(
                        analysis(),
                        null
                )
        )
                .isInstanceOf(
                        NullPointerException.class
                )
                .hasMessage(
                        "evaluatedMatch must not be null"
                );
    }

    @Test
    void shouldRejectMissingFingerprintGenerator() {
        assertThatThrownBy(() ->
                new FindingFactory(null)
        )
                .isInstanceOf(
                        NullPointerException.class
                )
                .hasMessage(
                        "fingerprintGenerator must not be null"
                );
    }

    private EvaluatedRuleMatch evaluatedMatch(
            Severity severity
    ) {
        RuleMetadata metadata =
                new RuleMetadata(
                        "CF-SEC-001",
                        "1.0.0",
                        "Hardcoded Secret",
                        FindingCategory.SECRETS,
                        Severity.CRITICAL,
                        "Detects secrets embedded directly in source code.",
                        "Exposed credentials may allow unauthorized access.",
                        "Move the secret to secure external configuration."
                );

        RuleMatch match =
                new RuleMatch(
                        "CF-SEC-001",
                        severity,
                        "src/main/java/Config.java",
                        2,
                        2,
                        "String password = \"********\";"
                );

        return new EvaluatedRuleMatch(
                metadata,
                match
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