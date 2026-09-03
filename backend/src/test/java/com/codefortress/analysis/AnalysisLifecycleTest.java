package com.codefortress.analysis;

import com.codefortress.identity.user.User;
import com.codefortress.project.Project;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AnalysisLifecycleTest {

    @Test
    void shouldStartQueuedAnalysis() {
        Analysis analysis = createAnalysis();

        analysis.start();

        assertThat(analysis.getStatus())
                .isEqualTo(AnalysisStatus.RUNNING);
        assertThat(analysis.getStartedAt()).isNotNull();
        assertThat(analysis.getCompletedAt()).isNull();
    }

    @Test
    void shouldCompleteRunningAnalysis() {
        Analysis analysis = createAnalysis();

        analysis.start();

        analysis.complete(
                82,
                12,
                450,
                3
        );

        assertThat(analysis.getStatus())
                .isEqualTo(AnalysisStatus.COMPLETED);
        assertThat(analysis.getSecurityScore())
                .isEqualTo((short) 82);
        assertThat(analysis.getFilesScanned())
                .isEqualTo(12);
        assertThat(analysis.getLinesScanned())
                .isEqualTo(450L);
        assertThat(analysis.getFindingsCount())
                .isEqualTo(3);
        assertThat(analysis.getStartedAt()).isNotNull();
        assertThat(analysis.getCompletedAt()).isNotNull();
        assertThat(analysis.getFailureCode()).isNull();
        assertThat(analysis.getFailureMessage()).isNull();
    }

    @Test
    void shouldFailRunningAnalysisWithSafeMessage() {
        Analysis analysis = createAnalysis();

        analysis.start();

        analysis.fail(
                "SOURCE_DISCOVERY_FAILED",
                "The source files could not be discovered"
        );

        assertThat(analysis.getStatus())
                .isEqualTo(AnalysisStatus.FAILED);
        assertThat(analysis.getFailureCode())
                .isEqualTo("SOURCE_DISCOVERY_FAILED");
        assertThat(analysis.getFailureMessage())
                .isEqualTo(
                        "The source files could not be discovered"
                );
        assertThat(analysis.getStartedAt()).isNotNull();
        assertThat(analysis.getCompletedAt()).isNotNull();
    }

    @Test
    void shouldCancelQueuedAnalysis() {
        Analysis analysis = createAnalysis();

        analysis.cancel();

        assertThat(analysis.getStatus())
                .isEqualTo(AnalysisStatus.CANCELLED);
        assertThat(analysis.getCompletedAt()).isNotNull();
    }

    @Test
    void shouldRejectInvalidMetricsAndTransitions() {
        Analysis analysis = createAnalysis();

        assertThatThrownBy(() ->
                analysis.complete(
                        80,
                        10,
                        100,
                        2
                )
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Analysis must be RUNNING");

        analysis.start();

        assertThatThrownBy(() ->
                analysis.complete(
                        101,
                        10,
                        100,
                        2
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "securityScore must be between 0 and 100"
                );

        assertThat(analysis.getStatus())
                .isEqualTo(AnalysisStatus.RUNNING);

        analysis.complete(
                80,
                10,
                100,
                2
        );

        assertThatThrownBy(analysis::start)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Analysis must be QUEUED");
    }

    private Analysis createAnalysis() {
        User owner = User.create(
                "owner@example.com",
                "test-password-hash",
                "Project Owner"
        );

        Project project = Project.create(
                owner,
                "CodeFortress API",
                "Project used during automated tests"
        );

        return Analysis.queueUpload(
                project,
                1,
                "0123456789abcdef0123456789abcdef"
                        + "0123456789abcdef0123456789abcdef",
                "source.zip",
                "rules-v1",
                "score-v1"
        );
    }
}