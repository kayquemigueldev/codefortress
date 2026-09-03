package com.codefortress.analysis.listing;

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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class ListAnalysesServiceTest {

    private static final String SOURCE_HASH =
            "0123456789abcdef0123456789abcdef"
                    + "0123456789abcdef0123456789abcdef";

    @Autowired
    private ListAnalysesService listAnalysesService;

    @Autowired
    private AnalysisRepository analysisRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldListNewestAnalysisFirst() {
        User owner = createUser(
                "owner@example.com",
                "Project Owner"
        );

        Project project = createProject(
                owner,
                "CodeFortress API"
        );

        Analysis first = createAnalysis(
                project,
                1,
                "first.zip"
        );

        Analysis second = createAnalysis(
                project,
                2,
                "second.zip"
        );

        second.start();
        second.complete(
                90,
                12,
                450,
                2
        );

        analysisRepository.saveAndFlush(second);

        List<ListedAnalysis> analyses =
                listAnalysesService.list(
                        owner.getId(),
                        project.getId()
                );

        assertThat(analyses)
                .extracting(ListedAnalysis::id)
                .containsExactly(
                        second.getId(),
                        first.getId()
                );

        assertThat(analyses.getFirst().status())
                .isEqualTo(AnalysisStatus.COMPLETED);
        assertThat(analyses.getFirst().securityScore())
                .isEqualTo((short) 90);
        assertThat(analyses.getFirst().filesScanned())
                .isEqualTo(12);
        assertThat(analyses.getFirst().linesScanned())
                .isEqualTo(450L);
        assertThat(analyses.getFirst().findingsCount())
                .isEqualTo(2);
    }

    @Test
    void shouldReturnEmptyHistoryForProjectWithoutAnalyses() {
        User owner = createUser(
                "owner@example.com",
                "Project Owner"
        );

        Project project = createProject(
                owner,
                "Empty Project"
        );

        assertThat(
                listAnalysesService.list(
                        owner.getId(),
                        project.getId()
                )
        ).isEmpty();
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

        createAnalysis(
                project,
                1,
                "private.zip"
        );

        assertThatThrownBy(() ->
                listAnalysesService.list(
                        anotherUser.getId(),
                        project.getId()
                )
        ).isInstanceOf(ProjectNotFoundException.class);
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

        createAnalysis(
                project,
                1,
                "archived.zip"
        );

        project.archive();
        projectRepository.saveAndFlush(project);

        assertThatThrownBy(() ->
                listAnalysesService.list(
                        owner.getId(),
                        project.getId()
                )
        ).isInstanceOf(ProjectNotFoundException.class);
    }

    private Analysis createAnalysis(
            Project project,
            int sequenceNumber,
            String filename
    ) {
        Analysis analysis = Analysis.queueUpload(
                project,
                sequenceNumber,
                SOURCE_HASH,
                filename,
                "rules-v1",
                "score-v1"
        );

        return analysisRepository.saveAndFlush(analysis);
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