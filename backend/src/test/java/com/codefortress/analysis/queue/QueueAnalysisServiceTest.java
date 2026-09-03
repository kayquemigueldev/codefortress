package com.codefortress.analysis.queue;

import com.codefortress.analysis.Analysis;
import com.codefortress.analysis.AnalysisRepository;
import com.codefortress.analysis.AnalysisStatus;
import com.codefortress.identity.user.User;
import com.codefortress.identity.user.UserRepository;
import com.codefortress.project.Project;
import com.codefortress.project.ProjectRepository;
import com.codefortress.project.details.ProjectNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class QueueAnalysisServiceTest {

    private static final String SOURCE_HASH =
            "0123456789abcdef0123456789abcdef"
                    + "0123456789abcdef0123456789abcdef";

    @Autowired
    private QueueAnalysisService queueAnalysisService;

    @Autowired
    private AnalysisRepository analysisRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldQueueFirstProjectAnalysis() {
        User owner = createUser(
                "owner@example.com",
                "Project Owner"
        );

        Project project = createProject(
                owner,
                "CodeFortress API"
        );

        QueuedAnalysis queuedAnalysis = queueAnalysisService.queue(
                owner.getId(),
                project.getId(),
                new QueueAnalysisCommand(
                        SOURCE_HASH,
                        "codefortress.zip"
                )
        );

        Analysis persistedAnalysis = analysisRepository
                .findById(queuedAnalysis.id())
                .orElseThrow();

        assertThat(queuedAnalysis.projectId())
                .isEqualTo(project.getId());
        assertThat(queuedAnalysis.sequenceNumber()).isEqualTo(1);
        assertThat(queuedAnalysis.status())
                .isEqualTo(AnalysisStatus.QUEUED);
        assertThat(queuedAnalysis.sourceFilename())
                .isEqualTo("codefortress.zip");
        assertThat(queuedAnalysis.createdAt()).isNotNull();

        assertThat(persistedAnalysis.getStatus())
                .isEqualTo(AnalysisStatus.QUEUED);
        assertThat(persistedAnalysis.getSourceReference())
                .isEqualTo(SOURCE_HASH);
    }

    @Test
    void shouldIncrementSequenceInsideProject() {
        User owner = createUser(
                "owner@example.com",
                "Project Owner"
        );

        Project project = createProject(
                owner,
                "CodeFortress API"
        );

        QueuedAnalysis first = queueAnalysisService.queue(
                owner.getId(),
                project.getId(),
                command("first.zip")
        );

        QueuedAnalysis second = queueAnalysisService.queue(
                owner.getId(),
                project.getId(),
                command("second.zip")
        );

        assertThat(first.sequenceNumber()).isEqualTo(1);
        assertThat(second.sequenceNumber()).isEqualTo(2);
        assertThat(analysisRepository.count()).isEqualTo(2);
    }

    @Test
    void shouldHideProjectOwnedByAnotherUser() {
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

        assertThatThrownBy(() ->
                queueAnalysisService.queue(
                        anotherUser.getId(),
                        project.getId(),
                        command("private.zip")
                )
        ).isInstanceOf(ProjectNotFoundException.class);

        assertThat(analysisRepository.count()).isZero();
    }

    @Test
    void shouldRejectArchivedProject() {
        User owner = createUser(
                "owner@example.com",
                "Project Owner"
        );

        Project project = createProject(
                owner,
                "Archived Project"
        );

        project.archive();
        projectRepository.saveAndFlush(project);

        assertThatThrownBy(() ->
                queueAnalysisService.queue(
                        owner.getId(),
                        project.getId(),
                        command("archived.zip")
                )
        ).isInstanceOf(ProjectNotFoundException.class);

        assertThat(analysisRepository.count()).isZero();
    }

    @Test
    void shouldRejectInvalidSourceHash() {
        assertThatThrownBy(() ->
                new QueueAnalysisCommand(
                        "not-a-sha-256-hash",
                        "source.zip"
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "sourceReference must be a lowercase SHA-256 hash"
                );
    }

    private QueueAnalysisCommand command(String filename) {
        return new QueueAnalysisCommand(
                SOURCE_HASH,
                filename
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