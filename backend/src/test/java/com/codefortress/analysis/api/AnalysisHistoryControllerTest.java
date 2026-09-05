package com.codefortress.analysis.api;

import com.codefortress.analysis.Analysis;
import com.codefortress.analysis.AnalysisRepository;
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
class AnalysisHistoryControllerTest {

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

    @Test
    void shouldListAnalysisHistoryNewestFirst()
            throws Exception {
        AuthenticatedUser owner =
                createAuthenticatedUser(
                        "Project Owner",
                        "owner@example.com"
                );

        Project project = createProject(
                owner.user(),
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
                94,
                15,
                620,
                2
        );

        analysisRepository.saveAndFlush(second);

        mockMvc.perform(
                        get(
                                "/api/v1/projects/{projectId}/analyses",
                                project.getId()
                        )
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        bearer(owner)
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()")
                        .value(2))
                .andExpect(jsonPath("$[0].id")
                        .value(second.getId().toString()))
                .andExpect(jsonPath("$[0].sequenceNumber")
                        .value(2))
                .andExpect(jsonPath("$[0].status")
                        .value("COMPLETED"))
                .andExpect(jsonPath("$[0].sourceType")
                        .value("UPLOAD"))
                .andExpect(jsonPath("$[0].sourceFilename")
                        .value("second.zip"))
                .andExpect(jsonPath("$[0].securityScore")
                        .value(94))
                .andExpect(jsonPath("$[0].filesScanned")
                        .value(15))
                .andExpect(jsonPath("$[0].linesScanned")
                        .value(620))
                .andExpect(jsonPath("$[0].findingsCount")
                        .value(2))
                .andExpect(jsonPath("$[0].startedAt")
                        .isNotEmpty())
                .andExpect(jsonPath("$[0].completedAt")
                        .isNotEmpty())
                .andExpect(jsonPath("$[1].id")
                        .value(first.getId().toString()))
                .andExpect(jsonPath("$[1].status")
                        .value("QUEUED"));
    }

    @Test
    void shouldReturnEmptyAnalysisHistory()
            throws Exception {
        AuthenticatedUser owner =
                createAuthenticatedUser(
                        "Project Owner",
                        "owner@example.com"
                );

        Project project = createProject(
                owner.user(),
                "Empty Project"
        );

        mockMvc.perform(
                        get(
                                "/api/v1/projects/{projectId}/analyses",
                                project.getId()
                        )
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        bearer(owner)
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()")
                        .value(0));
    }

    @Test
    void shouldHideHistoryFromAnotherUser()
            throws Exception {
        AuthenticatedUser owner =
                createAuthenticatedUser(
                        "Project Owner",
                        "owner@example.com"
                );

        AuthenticatedUser anotherUser =
                createAuthenticatedUser(
                        "Another User",
                        "another@example.com"
                );

        Project project = createProject(
                owner.user(),
                "Private Project"
        );

        createAnalysis(
                project,
                1,
                "private.zip"
        );

        mockMvc.perform(
                        get(
                                "/api/v1/projects/{projectId}/analyses",
                                project.getId()
                        )
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        bearer(anotherUser)
                                )
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code")
                        .value("PROJECT_NOT_FOUND"));
    }

    @Test
    void shouldRejectUnauthenticatedHistoryRequest()
            throws Exception {
        User owner = createUser(
                "owner@example.com",
                "Project Owner"
        );

        Project project = createProject(
                owner,
                "Protected Project"
        );

        mockMvc.perform(
                        get(
                                "/api/v1/projects/{projectId}/analyses",
                                project.getId()
                        )
                )
                .andExpect(status().isUnauthorized());
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

        LoginResult loginResult = loginService.login(
                new LoginCommand(
                        email,
                        PASSWORD
                )
        );

        User user = userRepository
                .findByEmail(email)
                .orElseThrow();

        return new AuthenticatedUser(
                user,
                loginResult.accessToken().value()
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

    private String bearer(
            AuthenticatedUser authenticatedUser
    ) {
        return "Bearer "
                + authenticatedUser.accessToken();
    }

    private record AuthenticatedUser(
            User user,
            String accessToken
    ) {
    }
}