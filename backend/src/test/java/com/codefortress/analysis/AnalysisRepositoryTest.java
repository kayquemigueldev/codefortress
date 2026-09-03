package com.codefortress.analysis;

import com.codefortress.identity.user.User;
import com.codefortress.identity.user.UserRepository;
import com.codefortress.project.Project;
import com.codefortress.project.ProjectRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class AnalysisRepositoryTest {

    private static final String SOURCE_HASH =
            "0123456789abcdef0123456789abcdef"
                    + "0123456789abcdef0123456789abcdef";

    @Autowired
    private AnalysisRepository analysisRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldPersistQueuedUploadAnalysis() {
        User owner = createUser(
                "owner@example.com",
                "Project Owner"
        );

        Project project = createProject(
                owner,
                "CodeFortress API"
        );

        Analysis analysis = Analysis.queueUpload(
                project,
                1,
                SOURCE_HASH,
                "codefortress.zip",
                "rules-v1",
                "score-v1"
        );

        Analysis persistedAnalysis =
                analysisRepository.saveAndFlush(analysis);

        assertThat(persistedAnalysis.getId()).isNotNull();
        assertThat(persistedAnalysis.getProject().getId())
                .isEqualTo(project.getId());
        assertThat(persistedAnalysis.getSequenceNumber()).isEqualTo(1);
        assertThat(persistedAnalysis.getStatus())
                .isEqualTo(AnalysisStatus.QUEUED);
        assertThat(persistedAnalysis.getSourceType())
                .isEqualTo(AnalysisSourceType.UPLOAD);
        assertThat(persistedAnalysis.getSourceReference())
                .isEqualTo(SOURCE_HASH);
        assertThat(persistedAnalysis.getSourceFilename())
                .isEqualTo("codefortress.zip");
        assertThat(persistedAnalysis.getRuleSetVersion())
                .isEqualTo("rules-v1");
        assertThat(persistedAnalysis.getScoreVersion())
                .isEqualTo("score-v1");
        assertThat(persistedAnalysis.getCreatedAt()).isNotNull();
        assertThat(persistedAnalysis.getStartedAt()).isNull();
        assertThat(persistedAnalysis.getCompletedAt()).isNull();
    }

    @Test
    void shouldFindLatestProjectAnalysis() {
        User owner = createUser(
                "owner@example.com",
                "Project Owner"
        );

        Project project = createProject(
                owner,
                "CodeFortress API"
        );

        analysisRepository.saveAndFlush(
                createAnalysis(project, 1, "first.zip")
        );

        analysisRepository.saveAndFlush(
                createAnalysis(project, 2, "second.zip")
        );

        Analysis latestAnalysis = analysisRepository
                .findTopByProject_IdOrderBySequenceNumberDesc(
                        project.getId()
                )
                .orElseThrow();

        assertThat(latestAnalysis.getSequenceNumber()).isEqualTo(2);
        assertThat(latestAnalysis.getSourceFilename())
                .isEqualTo("second.zip");
    }

    @Test
    void shouldIsolateAnalysisByProjectOwner() {
        User owner = createUser(
                "owner@example.com",
                "Project Owner"
        );

        User anotherUser = createUser(
                "another@example.com",
                "Another User"
        );

        Project project = createProject(
                owner,
                "Private Project"
        );

        Analysis analysis = analysisRepository.saveAndFlush(
                createAnalysis(project, 1, "private.zip")
        );

        assertThat(
                analysisRepository.findByIdAndProject_Owner_Id(
                        analysis.getId(),
                        owner.getId()
                )
        ).isPresent();

        assertThat(
                analysisRepository.findByIdAndProject_Owner_Id(
                        analysis.getId(),
                        anotherUser.getId()
                )
        ).isEmpty();
    }

    private Analysis createAnalysis(
            Project project,
            int sequenceNumber,
            String sourceFilename
    ) {
        return Analysis.queueUpload(
                project,
                sequenceNumber,
                SOURCE_HASH,
                sourceFilename,
                "rules-v1",
                "score-v1"
        );
    }

    private User createUser(
            String email,
            String displayName
    ) {
        User user = User.create(
                email,
                "test-password-hash",
                displayName
        );

        return userRepository.saveAndFlush(user);
    }

    private Project createProject(
            User owner,
            String name
    ) {
        Project project = Project.create(
                owner,
                name,
                "Project used during automated tests"
        );

        return projectRepository.saveAndFlush(project);
    }
}