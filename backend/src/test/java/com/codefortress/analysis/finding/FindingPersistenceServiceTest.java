package com.codefortress.analysis.finding;

import com.codefortress.analysis.Analysis;
import com.codefortress.analysis.AnalysisRepository;
import com.codefortress.analysis.Finding;
import com.codefortress.analysis.FindingCategory;
import com.codefortress.analysis.FindingRepository;
import com.codefortress.analysis.Severity;
import com.codefortress.analysis.engine.EvaluatedRuleMatch;
import com.codefortress.analysis.engine.FindingFactory;
import com.codefortress.analysis.engine.FindingFingerprintGenerator;
import com.codefortress.analysis.engine.RuleMatch;
import com.codefortress.analysis.engine.RuleMetadata;
import com.codefortress.analysis.lifecycle.AnalysisNotFoundException;
import com.codefortress.identity.user.User;
import com.codefortress.project.Project;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FindingPersistenceServiceTest {

    private final AnalysisRepository analysisRepository =
            mock(AnalysisRepository.class);

    private final FindingRepository findingRepository =
            mock(FindingRepository.class);

    private final FindingFactory findingFactory =
            new FindingFactory(
                    new FindingFingerprintGenerator()
            );

    private final FindingPersistenceService service =
            new FindingPersistenceService(
                    analysisRepository,
                    findingRepository,
                    findingFactory
            );

    @Test
    void shouldPersistEvaluatedRuleMatchesAsFindings() {
        UUID analysisId =
                UUID.randomUUID();

        Analysis analysis =
                analysis();

        when(
                analysisRepository.findById(
                        analysisId
                )
        ).thenReturn(
                Optional.of(analysis)
        );

        when(
                findingRepository.saveAllAndFlush(
                        anyList()
                )
        ).thenAnswer(
                invocation ->
                        invocation.getArgument(0)
        );

        int persistedCount =
                service.persist(
                        analysisId,
                        List.of(
                                evaluatedMatch()
                        )
                );

        assertThat(persistedCount)
                .isEqualTo(1);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Finding>> captor =
                ArgumentCaptor.forClass(
                        List.class
                );

        verify(
                findingRepository
        ).saveAllAndFlush(
                captor.capture()
        );

        List<Finding> savedFindings =
                captor.getValue();

        assertThat(savedFindings)
                .hasSize(1);

        Finding finding =
                savedFindings.getFirst();

        assertThat(finding.getAnalysis())
                .isSameAs(analysis);

        assertThat(finding.getRuleKey())
                .isEqualTo(
                        "CF-SEC-001"
                );

        assertThat(finding.getRuleVersion())
                .isEqualTo(
                        "1.0.0"
                );

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

        assertThat(finding.getFingerprint())
                .matches(
                        "[0-9a-f]{64}"
                );
    }

    @Test
    void shouldReturnZeroWhenThereAreNoMatches() {
        UUID analysisId =
                UUID.randomUUID();

        when(
                analysisRepository.findById(
                        analysisId
                )
        ).thenReturn(
                Optional.of(
                        analysis()
                )
        );

        int persistedCount =
                service.persist(
                        analysisId,
                        List.of()
                );

        assertThat(persistedCount)
                .isZero();

        verify(
                findingRepository,
                never()
        ).saveAllAndFlush(
                anyList()
        );
    }

    @Test
    void shouldRejectUnknownAnalysis() {
        UUID analysisId =
                UUID.randomUUID();

        when(
                analysisRepository.findById(
                        analysisId
                )
        ).thenReturn(
                Optional.empty()
        );

        assertThatThrownBy(() ->
                service.persist(
                        analysisId,
                        List.of(
                                evaluatedMatch()
                        )
                )
        )
                .isInstanceOf(
                        AnalysisNotFoundException.class
                );

        verify(
                findingRepository,
                never()
        ).saveAllAndFlush(
                anyList()
        );
    }

    @Test
    void shouldRejectMissingMatches() {
        assertThatThrownBy(() ->
                service.persist(
                        UUID.randomUUID(),
                        null
                )
        )
                .isInstanceOf(
                        NullPointerException.class
                )
                .hasMessage(
                        "matches must not be null"
                );
    }

    private EvaluatedRuleMatch evaluatedMatch() {
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
                        Severity.CRITICAL,
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