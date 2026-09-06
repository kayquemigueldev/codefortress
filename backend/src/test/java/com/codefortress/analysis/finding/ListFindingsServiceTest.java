package com.codefortress.analysis.finding;

import com.codefortress.analysis.Analysis;
import com.codefortress.analysis.AnalysisRepository;
import com.codefortress.analysis.Finding;
import com.codefortress.analysis.FindingCategory;
import com.codefortress.analysis.FindingRepository;
import com.codefortress.analysis.FindingStatus;
import com.codefortress.analysis.Severity;
import com.codefortress.analysis.lifecycle.AnalysisNotFoundException;
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
class ListFindingsServiceTest {

    private static final String SOURCE_HASH =
            "0123456789abcdef0123456789abcdef"
                    + "0123456789abcdef0123456789abcdef";

    @Autowired
    private ListFindingsService listFindingsService;

    @Autowired
    private FindingRepository findingRepository;

    @Autowired
    private AnalysisRepository analysisRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldListFindingsForAnalysis() {
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

        List<ListedFinding> findings =
                listFindingsService.list(
                        owner.getId(),
                        project.getId(),
                        analysis.getId()
                );

        assertThat(findings)
                .hasSize(1);

        ListedFinding listed =
                findings.getFirst();

        assertThat(listed.id())
                .isEqualTo(finding.getId());

        assertThat(listed.ruleKey())
                .isEqualTo("CF-SEC-001");

        assertThat(listed.ruleVersion())
                .isEqualTo("1.0.0");

        assertThat(listed.title())
                .isEqualTo("Hardcoded Secret");

        assertThat(listed.category())
                .isEqualTo(
                        FindingCategory.SECRETS
                );

        assertThat(listed.severity())
                .isEqualTo(
                        Severity.CRITICAL
                );

        assertThat(listed.status())
                .isEqualTo(
                        FindingStatus.OPEN
                );

        assertThat(listed.filePath())
                .isEqualTo(
                        "src/Config.java"
                );

        assertThat(listed.startLine())
                .isEqualTo(2);

        assertThat(listed.endLine())
                .isEqualTo(2);

        assertThat(listed.codeExcerpt())
                .contains("********");

        assertThat(listed.fingerprint())
                .isEqualTo(
                        "a".repeat(64)
                );

        assertThat(listed.createdAt())
                .isNotNull();

        assertThat(listed.statusUpdatedAt())
                .isNotNull();
    }

    @Test
    void shouldReturnEmptyListWhenAnalysisHasNoFindings() {
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

        assertThat(
                listFindingsService.list(
                        owner.getId(),
                        project.getId(),
                        analysis.getId()
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

        Analysis analysis =
                createAnalysis(
                        project,
                        1
                );

        assertThatThrownBy(() ->
                listFindingsService.list(
                        anotherUser.getId(),
                        project.getId(),
                        analysis.getId()
                )
        ).isInstanceOf(
                ProjectNotFoundException.class
        );
    }

    @Test
    void shouldRejectAnalysisFromAnotherProject() {
        User owner = createUser(
                "owner@example.com",
                "Project Owner"
        );

        Project firstProject =
                createProject(
                        owner,
                        "First Project"
                );

        Project secondProject =
                createProject(
                        owner,
                        "Second Project"
                );

        Analysis analysis =
                createAnalysis(
                        secondProject,
                        1
                );

        assertThatThrownBy(() ->
                listFindingsService.list(
                        owner.getId(),
                        firstProject.getId(),
                        analysis.getId()
                )
        ).isInstanceOf(
                AnalysisNotFoundException.class
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

        project.archive();
        projectRepository.saveAndFlush(project);

        assertThatThrownBy(() ->
                listFindingsService.list(
                        owner.getId(),
                        project.getId(),
                        analysis.getId()
                )
        ).isInstanceOf(
                ProjectNotFoundException.class
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