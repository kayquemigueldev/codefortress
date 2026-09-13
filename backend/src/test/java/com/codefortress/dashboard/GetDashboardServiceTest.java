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

        createCompletedAnalysis(
                secondActiveProject,
                1,
                "second-project.zip",
                90,
                0
        );

        createCompletedAnalysis(
                activeProject,
                1,
                "previous-codefortress.zip",
                95,
                0
        );

        Analysis latestAnalysis =
                createCompletedAnalysis(
                        activeProject,
                        2,
                        "codefortress.zip",
                        70,
                        4
                );

        Analysis archivedAnalysis =
                createCompletedAnalysis(
                        archivedProject,
                        1,
                        "archived.zip",
                        100,
                        1
                );

        Analysis anotherOwnerAnalysis =
                createCompletedAnalysis(
                        anotherOwnerProject,
                        1,
                        "other.zip",
                        10,
                        1
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
                latestAnalysis,
                Severity.MEDIUM,
                "c".repeat(64)
        );

        createFinding(
                latestAnalysis,
                Severity.LOW,
                "d".repeat(64)
        );

        createFinding(
                archivedAnalysis,
                Severity.CRITICAL,
                "e".repeat(64)
        );

        createFinding(
                anotherOwnerAnalysis,
                Severity.CRITICAL,
                "f".repeat(64)
        );

        DashboardOverview overview =
                getDashboardService.get(
                        owner.getId()
                );

        assertThat(
                overview.activeProjects()
        ).isEqualTo(2);

        assertThat(
                overview.averageSecurityScore()
        ).isEqualTo(80);

        assertThat(
                overview.openFindings()
        ).isEqualTo(4);

        assertThat(
                overview.criticalOpenFindings()
        ).isEqualTo(1);

        assertThat(
                overview.highOpenFindings()
        ).isEqualTo(1);

        assertThat(
                overview.mediumOpenFindings()
        ).isEqualTo(1);

        assertThat(
                overview.lowOpenFindings()
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
        ).isEqualTo(2);

        assertThat(
                overview.latestAnalysis().sourceFilename()
        ).isEqualTo(
                "codefortress.zip"
        );

        assertThat(
                overview.latestAnalysis().securityScore()
        ).isEqualTo((short) 70);

        assertThat(
                overview.latestAnalysis().findingsCount()
        ).isEqualTo(4);

        assertThat(
                overview.latestAnalysis().createdAt()
        ).isNotNull();

        assertThat(
                overview.latestAnalysis().completedAt()
        ).isNotNull();

        assertThat(
                overview.recentAnalyses()
        ).hasSize(3);

        assertThat(
                overview.recentAnalyses()
                        .getFirst()
                        .id()
        ).isEqualTo(
                latestAnalysis.getId()
        );

        assertThat(
                overview.recentAnalyses()
        )
                .extracting(
                        DashboardOverview.LatestAnalysis::projectName
                )
                .containsOnly(
                        "CodeFortress",
                        "Another Active Project"
                );

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

    private Analysis createCompletedAnalysis(
            Project project,
            int sequenceNumber,
            String filename,
            int securityScore,
            int findingsCount
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

        analysis =
                analysisRepository
                        .saveAndFlush(
                                analysis
                        );

        analysis.start();

        analysis.complete(
                securityScore,
                1,
                10L,
                findingsCount
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