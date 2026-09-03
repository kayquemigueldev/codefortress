package com.codefortress.analysis.lifecycle;

import com.codefortress.analysis.Analysis;
import com.codefortress.analysis.AnalysisRepository;
import com.codefortress.analysis.AnalysisStatus;
import com.codefortress.identity.user.User;
import com.codefortress.identity.user.UserRepository;
import com.codefortress.project.Project;
import com.codefortress.project.ProjectRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class AnalysisLifecycleServiceTest {

    private static final String SOURCE_HASH =
            "0123456789abcdef0123456789abcdef"
                    + "0123456789abcdef0123456789abcdef";

    @Autowired
    private AnalysisLifecycleService lifecycleService;

    @Autowired
    private AnalysisRepository analysisRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldStartPersistedAnalysis() {
        Analysis analysis = createAnalysis();

        AnalysisState state =
                lifecycleService.start(
                        analysis.getId()
                );

        assertThat(state.status())
                .isEqualTo(AnalysisStatus.RUNNING);
        assertThat(state.startedAt()).isNotNull();
        assertThat(state.completedAt()).isNull();

        assertThat(
                findAnalysis(analysis.getId()).getStatus()
        ).isEqualTo(AnalysisStatus.RUNNING);
    }

    @Test
    void shouldCompleteAnalysisWithMetrics() {
        Analysis analysis = createAnalysis();

        lifecycleService.start(analysis.getId());

        AnalysisState state =
                lifecycleService.complete(
                        analysis.getId(),
                        87,
                        24,
                        1_250,
                        6
                );

        assertThat(state.status())
                .isEqualTo(AnalysisStatus.COMPLETED);
        assertThat(state.securityScore())
                .isEqualTo((short) 87);
        assertThat(state.filesScanned())
                .isEqualTo(24);
        assertThat(state.linesScanned())
                .isEqualTo(1_250L);
        assertThat(state.findingsCount())
                .isEqualTo(6);
        assertThat(state.completedAt()).isNotNull();

        Analysis persisted =
                findAnalysis(analysis.getId());

        assertThat(persisted.getStatus())
                .isEqualTo(AnalysisStatus.COMPLETED);
        assertThat(persisted.getSecurityScore())
                .isEqualTo((short) 87);
    }

    @Test
    void shouldFailQueuedAnalysis() {
        Analysis analysis = createAnalysis();

        AnalysisState state = lifecycleService.fail(
                analysis.getId(),
                "SOURCE_EXTRACTION_FAILED",
                "The source archive could not be extracted"
        );

        assertThat(state.status())
                .isEqualTo(AnalysisStatus.FAILED);
        assertThat(state.failureCode())
                .isEqualTo("SOURCE_EXTRACTION_FAILED");
        assertThat(state.failureMessage())
                .isEqualTo(
                        "The source archive could not be extracted"
                );
        assertThat(state.startedAt()).isNotNull();
        assertThat(state.completedAt()).isNotNull();
    }

    @Test
    void shouldCancelQueuedAnalysis() {
        Analysis analysis = createAnalysis();

        AnalysisState state =
                lifecycleService.cancel(
                        analysis.getId()
                );

        assertThat(state.status())
                .isEqualTo(AnalysisStatus.CANCELLED);
        assertThat(state.completedAt()).isNotNull();
    }

    @Test
    void shouldRejectUnknownAnalysis() {
        UUID unknownAnalysisId =
                UUID.randomUUID();

        assertThatThrownBy(() ->
                lifecycleService.start(
                        unknownAnalysisId
                )
        ).isInstanceOf(
                AnalysisNotFoundException.class
        );
    }

    private Analysis createAnalysis() {
        User owner = User.create(
                "owner@example.com",
                "test-password-hash",
                "Project Owner"
        );

        userRepository.saveAndFlush(owner);

        Project project = Project.create(
                owner,
                "CodeFortress API",
                "Project used during automated tests"
        );

        projectRepository.saveAndFlush(project);

        Analysis analysis = Analysis.queueUpload(
                project,
                1,
                SOURCE_HASH,
                "source.zip",
                "rules-v1",
                "score-v1"
        );

        return analysisRepository.saveAndFlush(analysis);
    }

    private Analysis findAnalysis(UUID analysisId) {
        return analysisRepository
                .findById(analysisId)
                .orElseThrow();
    }
}