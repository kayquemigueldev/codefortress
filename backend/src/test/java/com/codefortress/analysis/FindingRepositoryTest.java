package com.codefortress.analysis;

import com.codefortress.identity.user.User;
import com.codefortress.identity.user.UserRepository;
import com.codefortress.project.Project;
import com.codefortress.project.ProjectRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class FindingRepositoryTest {

    @Autowired
    private FindingRepository findingRepository;

    @Autowired
    private AnalysisRepository analysisRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldPersistAndLoadFinding() {
        Analysis analysis =
                createAnalysis();

        Finding finding =
                Finding.create(
                        analysis,
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

        Finding persisted =
                findingRepository.saveAndFlush(
                        finding
                );

        assertThat(persisted.getId())
                .isNotNull();

        assertThat(persisted.getCreatedAt())
                .isNotNull();

        assertThat(persisted.getStatusUpdatedAt())
                .isNotNull();

        List<Finding> findings =
                findingRepository
                        .findAllByAnalysis_IdOrderByCreatedAtAsc(
                                analysis.getId()
                        );

        assertThat(findings)
                .hasSize(1);

        Finding loaded =
                findings.getFirst();

        assertThat(loaded.getRuleKey())
                .isEqualTo("CF-SEC-001");

        assertThat(loaded.getRuleVersion())
                .isEqualTo("1.0.0");

        assertThat(loaded.getFingerprint())
                .isEqualTo(
                        "a".repeat(64)
                );

        assertThat(loaded.getCategory())
                .isEqualTo(
                        FindingCategory.SECRETS
                );

        assertThat(loaded.getSeverity())
                .isEqualTo(
                        Severity.CRITICAL
                );

        assertThat(loaded.getStatus())
                .isEqualTo(
                        FindingStatus.OPEN
                );

        assertThat(loaded.getFilePath())
                .isEqualTo(
                        "src/main/java/Config.java"
                );
    }

    @Test
    void shouldFindExistingFingerprintWithinAnalysis() {
        Analysis analysis =
                createAnalysis();

        String fingerprint =
                "b".repeat(64);

        Finding finding =
                Finding.create(
                        analysis,
                        "CF-SEC-001",
                        "1.0.0",
                        fingerprint,
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
                );

        findingRepository.saveAndFlush(
                finding
        );

        assertThat(
                findingRepository
                        .existsByAnalysis_IdAndFingerprint(
                                analysis.getId(),
                                fingerprint
                        )
        ).isTrue();

        assertThat(
                findingRepository
                        .existsByAnalysis_IdAndFingerprint(
                                analysis.getId(),
                                "c".repeat(64)
                        )
        ).isFalse();
    }

    private Analysis createAnalysis() {
        User owner =
                userRepository.saveAndFlush(
                        User.create(
                                "owner@example.com",
                                "test-password-hash",
                                "Project Owner"
                        )
                );

        Project project =
                projectRepository.saveAndFlush(
                        Project.create(
                                owner,
                                "CodeFortress API",
                                "Project used during automated tests"
                        )
                );

        return analysisRepository.saveAndFlush(
                Analysis.queueUpload(
                        project,
                        1,
                        "source-archive-reference",
                        "source.zip",
                        "ruleset-java-v1",
                        "score-v1"
                )
        );
    }
}