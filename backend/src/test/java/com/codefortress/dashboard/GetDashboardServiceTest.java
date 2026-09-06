package com.codefortress.dashboard;

import com.codefortress.analysis.Analysis;
import com.codefortress.analysis.AnalysisRepository;
import com.codefortress.analysis.Finding;
import com.codefortress.analysis.FindingCategory;
import com.codefortress.analysis.FindingRepository;
import com.codefortress.analysis.Severity;
import com.codefortress.identity.user.CurrentUserUnavailableException;
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
class GetDashboardServiceTest {

    private static final String SOURCE_HASH =
            "0123456789abcdef0123456789abcdef"
                    + "0123456789abcdef0123456789abcdef";

    @Autowired
    private GetDashboardService getDashboardService;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private AnalysisRepository analysisRepository;

    @Autowired
    private FindingRepository findingRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldReturnDashboardOverviewForOwner() {
        User owner = createUser(
                "owner@example.com",
                "Project Owner"
        );

        User anotherOwner = createUser(
                "another@example.com",
                "Another Owner"
        );

        Project activeProject = createProject(
                owner,
                "CodeFortress"
        );

        Project secondActiveProject = createProject(
                owner,
                "Another Active Project"
        );

        Project archivedProject = createProject(
                owner,
                "Archived Project"
        );

        archivedProject.archive();
        projectRepository.saveAndFlush(
                archivedProject
        );

        Project anotherOwnerProject = createProject(
                anotherOwner,
                "Another Owner Project"
        );

        Analysis latestAnalysis = createAnalysis(
                activeProject,
                1,
                "codefortress.zip"
        );

        Analysis archivedAnalysis = createAnalysis(
                archivedProject,
                1,
                "archived.zip"
        );

        Analysis anotherOwnerAnalysis = createAnalysis(
                anotherOwnerProject,
                1,
                "other.zip"
        );

        createFinding(
                latestAnalysis,
                Severity.CRITICAL,
                "a".repeat(64)
        );

        createFinding(
                latestAnalysis,
                Severity.HIGH,
                "b".repeat(64)
        );

        createFinding(
                archivedAnalysis,
                Severity.CRITICAL,
                "c".repeat(64)
        );

        createFinding(
                anotherOwnerAnalysis,
                Severity.CRITICAL,
                "d".repeat(64)
        );

        DashboardOverview overview =
                getDashboardService.get(
                        owner.getId()
                );

        assertThat(
                overview.activeProjects()
        ).isEqualTo(2);

        assertThat(
                overview.openFindings()
        ).isEqualTo(2);

        assertThat(
                overview.criticalOpenFindings()
        ).isEqualTo(1);

        assertThat(
                overview.latestAnalysis()
        ).isNotNull();

        assertThat(
                overview.latestAnalysis().id()
        ).isEqualTo(
                latestAnalysis.getId()
        );

        assertThat(
                overview.latestAnalysis().projectId()
        ).isEqualTo(
                activeProject.getId()
        );

        assertThat(
                overview.latestAnalysis().projectName()
        ).isEqualTo(
                "CodeFortress"
        );

        assertThat(
                overview.latestAnalysis().sequenceNumber()
        ).isEqualTo(1);

        assertThat(
                overview.latestAnalysis().sourceFilename()
        ).isEqualTo(
                "codefortress.zip"
        );

        assertThat(
                overview.latestAnalysis().status()
        ).isEqualTo(
                latestAnalysis.getStatus()
        );

        assertThat(
                overview.latestAnalysis().securityScore()
        ).isNull();

        assertThat(
                overview.latestAnalysis().findingsCount()
        ).isNull();

        assertThat(
                overview.latestAnalysis().createdAt()
        ).isNotNull();

        assertThat(
                overview.latestAnalysis().completedAt()
        ).isNull();

        assertThat(
                secondActiveProject.getId()
        ).isNotNull();
    }

    @Test
    void shouldReturnNullLatestAnalysisWhenOwnerHasNoAnalyses() {
        User owner = createUser(
                "owner@example.com",
                "Project Owner"
        );

        createProject(
                owner,
                "CodeFortress"
        );

        DashboardOverview overview =
                getDashboardService.get(
                        owner.getId()
                );

        assertThat(
                overview.activeProjects()
        ).isEqualTo(1);

        assertThat(
                overview.openFindings()
        ).isZero();

        assertThat(
                overview.criticalOpenFindings()
        ).isZero();

        assertThat(
                overview.latestAnalysis()
        ).isNull();
    }

    @Test
    void shouldRejectUnavailableOwner() {
        UUID unavailableOwnerId =
                UUID.randomUUID();

        assertThatThrownBy(() ->
                getDashboardService.get(
                        unavailableOwnerId
                )
        ).isInstanceOf(
                CurrentUserUnavailableException.class
        );
    }

    private Finding createFinding(
            Analysis analysis,
            Severity severity,
            String fingerprint
    ) {
        Finding finding =
                Finding.create(
                        analysis,
                        "CF-SEC-001",
                        "1.0.0",
                        fingerprint,
                        "Security Finding",
                        FindingCategory.SECRETS,
                        severity,
                        "src/Config.java",
                        2,
                        2,
                        "String secret = \"********\";",
                        "Security issue detected.",
                        "Sensitive data may be exposed.",
                        "Move the secret to a secure store."
                );

        return findingRepository
                .saveAndFlush(
                        finding
                );
    }

    private Analysis createAnalysis(
            Project project,
            int sequenceNumber,
            String filename
    ) {
        Analysis analysis =
                Analysis.queueUpload(
                        project,
                        sequenceNumber,
                        SOURCE_HASH,
                        filename,
                        "rules-v1",
                        "score-v1"
                );

        return analysisRepository
                .saveAndFlush(
                        analysis
                );
    }

    private Project createProject(
            User owner,
            String name
    ) {
        Project project =
                Project.create(
                        owner,
                        name,
                        "Project used during dashboard tests"
                );

        return projectRepository
                .saveAndFlush(
                        project
                );
    }

    private User createUser(
            String email,
            String displayName
    ) {
        User user =
                User.create(
                        email,
                        "test-password-hash",
                        displayName
                );

        return userRepository
                .saveAndFlush(
                        user
                );
    }
}