package com.codefortress.analysis.finding;

import com.codefortress.analysis.Analysis;
import com.codefortress.analysis.AnalysisRepository;
import com.codefortress.analysis.Finding;
import com.codefortress.analysis.FindingCategory;
import com.codefortress.analysis.FindingRepository;
import com.codefortress.analysis.FindingStatus;
import com.codefortress.analysis.Severity;
import com.codefortress.identity.user.User;
import com.codefortress.identity.user.UserRepository;
import com.codefortress.project.Project;
import com.codefortress.project.ProjectRepository;
import com.codefortress.project.details.ProjectNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class UpdateFindingStatusServiceTest {

    private static final String SOURCE_HASH =
            "0123456789abcdef0123456789abcdef"
                    + "0123456789abcdef0123456789abcdef";

    @Autowired
    private UpdateFindingStatusService updateFindingStatusService;

    @Autowired
    private FindingRepository findingRepository;

    @Autowired
    private AnalysisRepository analysisRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldUpdateFindingStatus() {
        User owner = createUser(
                "owner@example.com",
                "Project Owner"
        );

        Project project = createProject(
                owner,
                "CodeFortress API"
        );

        Analysis analysis =
                createAnalysis(
                        project,
                        1
                );

        Finding finding =
                createFinding(
                        analysis,
                        "a".repeat(64)
                );

        Instant previousStatusUpdatedAt =
                finding.getStatusUpdatedAt();

        ListedFinding updated =
                updateFindingStatusService.update(
                        owner.getId(),
                        project.getId(),
                        analysis.getId(),
                        finding.getId(),
                        FindingStatus.RESOLVED
                );

        findingRepository.flush();

        Finding persistedFinding =
                findingRepository
                        .findById(finding.getId())
                        .orElseThrow();

        assertThat(updated.status())
                .isEqualTo(
                        FindingStatus.RESOLVED
                );

        assertThat(persistedFinding.getStatus())
                .isEqualTo(
                        FindingStatus.RESOLVED
                );

        assertThat(
                persistedFinding
                        .getStatusUpdatedAt()
                        .compareTo(
                                previousStatusUpdatedAt
                        )
        )
                .isGreaterThanOrEqualTo(0);
    }

    @Test
    void shouldKeepTimestampWhenStatusDoesNotChange() {
        User owner = createUser(
                "owner@example.com",
                "Project Owner"
        );

        Project project = createProject(
                owner,
                "CodeFortress API"
        );

        Analysis analysis =
                createAnalysis(
                        project,
                        1
                );

        Finding finding =
                createFinding(
                        analysis,
                        "b".repeat(64)
                );

        Instant previousStatusUpdatedAt =
                finding.getStatusUpdatedAt();

        ListedFinding updated =
                updateFindingStatusService.update(
                        owner.getId(),
                        project.getId(),
                        analysis.getId(),
                        finding.getId(),
                        FindingStatus.OPEN
                );

        assertThat(updated.status())
                .isEqualTo(
                        FindingStatus.OPEN
                );

        assertThat(
                updated.statusUpdatedAt()
        ).isEqualTo(
                previousStatusUpdatedAt
        );
    }

    @Test
    void shouldRejectFindingFromAnotherAnalysis() {
        User owner = createUser(
                "owner@example.com",
                "Project Owner"
        );

        Project project = createProject(
                owner,
                "CodeFortress API"
        );

        Analysis firstAnalysis =
                createAnalysis(
                        project,
                        1
                );

        Analysis secondAnalysis =
                createAnalysis(
                        project,
                        2
                );

        Finding finding =
                createFinding(
                        secondAnalysis,
                        "c".repeat(64)
                );

        assertThatThrownBy(() ->
                updateFindingStatusService.update(
                        owner.getId(),
                        project.getId(),
                        firstAnalysis.getId(),
                        finding.getId(),
                        FindingStatus.RESOLVED
                )
        ).isInstanceOf(
                FindingNotFoundException.class
        );

        assertThat(
                finding.getStatus()
        ).isEqualTo(
                FindingStatus.OPEN
        );
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

        Analysis analysis =
                createAnalysis(
                        project,
                        1
                );

        Finding finding =
                createFinding(
                        analysis,
                        "d".repeat(64)
                );

        assertThatThrownBy(() ->
                updateFindingStatusService.update(
                        anotherUser.getId(),
                        project.getId(),
                        analysis.getId(),
                        finding.getId(),
                        FindingStatus.RESOLVED
                )
        ).isInstanceOf(
                ProjectNotFoundException.class
        );

        assertThat(
                finding.getStatus()
        ).isEqualTo(
                FindingStatus.OPEN
        );
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

        Analysis analysis =
                createAnalysis(
                        project,
                        1
                );

        Finding finding =
                createFinding(
                        analysis,
                        "e".repeat(64)
                );

        project.archive();
        projectRepository.saveAndFlush(project);

        assertThatThrownBy(() ->
                updateFindingStatusService.update(
                        owner.getId(),
                        project.getId(),
                        analysis.getId(),
                        finding.getId(),
                        FindingStatus.RESOLVED
                )
        ).isInstanceOf(
                ProjectNotFoundException.class
        );

        assertThat(
                finding.getStatus()
        ).isEqualTo(
                FindingStatus.OPEN
        );
    }

    private Finding createFinding(
            Analysis analysis,
            String fingerprint
    ) {
        Finding finding =
                Finding.create(
                        analysis,
                        "CF-SEC-001",
                        "1.0.0",
                        fingerprint,
                        "Hardcoded Secret",
                        FindingCategory.SECRETS,
                        Severity.CRITICAL,
                        "src/Config.java",
                        2,
                        2,
                        "String password = \"********\";",
                        "Secret embedded in source code.",
                        "Credentials may be exposed.",
                        "Move the secret to a secure store."
                );

        return findingRepository
                .saveAndFlush(
                        finding
                );
    }

    private Analysis createAnalysis(
            Project project,
            int sequenceNumber
    ) {
        Analysis analysis =
                Analysis.queueUpload(
                        project,
                        sequenceNumber,
                        SOURCE_HASH,
                        "source.zip",
                        "rules-v1",
                        "score-v1"
                );

        return analysisRepository
                .saveAndFlush(
                        analysis
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

    private Project createProject(
            User owner,
            String name
    ) {
        Project project =
                Project.create(
                        owner,
                        name,
                        "Project used during automated tests"
                );

        return projectRepository
                .saveAndFlush(
                        project
                );
    }
}