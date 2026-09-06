package com.codefortress.dashboard.api;

import com.codefortress.analysis.Analysis;
import com.codefortress.analysis.AnalysisRepository;
import com.codefortress.analysis.Finding;
import com.codefortress.analysis.FindingCategory;
import com.codefortress.analysis.FindingRepository;
import com.codefortress.analysis.Severity;
import com.codefortress.identity.authentication.LoginCommand;
import com.codefortress.identity.authentication.LoginResult;
import com.codefortress.identity.authentication.LoginService;
import com.codefortress.identity.registration.RegisterUserCommand;
import com.codefortress.identity.registration.RegisterUserService;
import com.codefortress.identity.user.User;
import com.codefortress.identity.user.UserRepository;
import com.codefortress.project.Project;
import com.codefortress.project.ProjectRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class DashboardControllerTest {

    private static final String PASSWORD =
            "correct-password";

    private static final String SOURCE_HASH =
            "0123456789abcdef0123456789abcdef"
                    + "0123456789abcdef0123456789abcdef";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RegisterUserService registerUserService;

    @Autowired
    private LoginService loginService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private AnalysisRepository analysisRepository;

    @Autowired
    private FindingRepository findingRepository;

    @Test
    void shouldReturnDashboardForAuthenticatedUser()
            throws Exception {
        AuthenticatedUser owner =
                createAuthenticatedUser(
                        "Kayque Miguel",
                        "kayque@example.com"
                );

        Project project =
                createProject(
                        owner.user(),
                        "CodeFortress"
                );

        Analysis analysis =
                createAnalysis(
                        project,
                        1
                );

        createFinding(
                analysis
        );

        mockMvc.perform(
                        get("/api/v1/dashboard")
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        bearer(owner)
                                )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$.activeProjects")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$.openFindings")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$.criticalOpenFindings")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$.latestAnalysis.id")
                                .value(
                                        analysis.getId()
                                                .toString()
                                )
                )
                .andExpect(
                        jsonPath("$.latestAnalysis.projectId")
                                .value(
                                        project.getId()
                                                .toString()
                                )
                )
                .andExpect(
                        jsonPath("$.latestAnalysis.projectName")
                                .value("CodeFortress")
                )
                .andExpect(
                        jsonPath("$.latestAnalysis.sequenceNumber")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$.latestAnalysis.status")
                                .value("QUEUED")
                )
                .andExpect(
                        jsonPath("$.latestAnalysis.sourceFilename")
                                .value("source.zip")
                )
                .andExpect(
                        jsonPath("$.latestAnalysis.securityScore")
                                .doesNotExist()
                )
                .andExpect(
                        jsonPath("$.latestAnalysis.findingsCount")
                                .doesNotExist()
                )
                .andExpect(
                        jsonPath("$.latestAnalysis.createdAt")
                                .isNotEmpty()
                )
                .andExpect(
                        jsonPath("$.latestAnalysis.completedAt")
                                .doesNotExist()
                );
    }

    @Test
    void shouldReturnDashboardWithoutLatestAnalysis()
            throws Exception {
        AuthenticatedUser owner =
                createAuthenticatedUser(
                        "Kayque Miguel",
                        "kayque@example.com"
                );

        createProject(
                owner.user(),
                "CodeFortress"
        );

        mockMvc.perform(
                        get("/api/v1/dashboard")
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        bearer(owner)
                                )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$.activeProjects")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$.openFindings")
                                .value(0)
                )
                .andExpect(
                        jsonPath("$.criticalOpenFindings")
                                .value(0)
                )
                .andExpect(
                        jsonPath("$.latestAnalysis")
                                .doesNotExist()
                );
    }

    @Test
    void shouldRequireAuthentication()
            throws Exception {
        mockMvc.perform(
                        get("/api/v1/dashboard")
                )
                .andExpect(
                        status().isUnauthorized()
                );
    }

    private Finding createFinding(
            Analysis analysis
    ) {
        Finding finding =
                Finding.create(
                        analysis,
                        "CF-SEC-001",
                        "1.0.0",
                        "a".repeat(64),
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

    private AuthenticatedUser createAuthenticatedUser(
            String displayName,
            String email
    ) {
        registerUserService.register(
                new RegisterUserCommand(
                        displayName,
                        email,
                        PASSWORD
                )
        );

        LoginResult loginResult =
                loginService.login(
                        new LoginCommand(
                                email,
                                PASSWORD
                        )
                );

        User user =
                userRepository
                        .findByEmail(email)
                        .orElseThrow();

        return new AuthenticatedUser(
                user,
                loginResult.accessToken().value()
        );
    }

    private String bearer(
            AuthenticatedUser user
    ) {
        return "Bearer "
                + user.accessToken();
    }

    private record AuthenticatedUser(
            User user,
            String accessToken
    ) {
    }
}