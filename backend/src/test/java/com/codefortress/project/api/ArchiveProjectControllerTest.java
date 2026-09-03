package com.codefortress.project.api;

import com.codefortress.identity.authentication.LoginCommand;
import com.codefortress.identity.authentication.LoginResult;
import com.codefortress.identity.authentication.LoginService;
import com.codefortress.identity.registration.RegisterUserCommand;
import com.codefortress.identity.registration.RegisterUserService;
import com.codefortress.identity.user.User;
import com.codefortress.identity.user.UserRepository;
import com.codefortress.project.Project;
import com.codefortress.project.ProjectRepository;
import com.codefortress.project.ProjectStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ArchiveProjectControllerTest {

    private static final String PASSWORD = "correct-password";

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

    @Test
    void shouldArchiveOwnedProject() throws Exception {
        AuthenticatedUser owner = createAuthenticatedUser(
                "Project Owner",
                "owner@example.com"
        );

        Project project = createProject(
                owner.user(),
                "CodeFortress API"
        );

        mockMvc.perform(delete("/api/v1/projects/{projectId}", project.getId())
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer " + owner.accessToken()
                        ))
                .andExpect(status().isNoContent());

        Project archivedProject = projectRepository
                .findById(project.getId())
                .orElseThrow();

        assertThat(archivedProject.getStatus())
                .isEqualTo(ProjectStatus.ARCHIVED);
    }

    @Test
    void shouldReturnNotFoundWhenProjectBelongsToAnotherUser()
            throws Exception {
        AuthenticatedUser owner = createAuthenticatedUser(
                "Project Owner",
                "owner@example.com"
        );

        AuthenticatedUser anotherUser = createAuthenticatedUser(
                "Another User",
                "another@example.com"
        );

        Project project = createProject(
                owner.user(),
                "Private Project"
        );

        mockMvc.perform(delete("/api/v1/projects/{projectId}", project.getId())
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer " + anotherUser.accessToken()
                        ))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code")
                        .value("PROJECT_NOT_FOUND"));

        Project persistedProject = projectRepository
                .findById(project.getId())
                .orElseThrow();

        assertThat(persistedProject.getStatus())
                .isEqualTo(ProjectStatus.ACTIVE);
    }

    @Test
    void shouldAllowArchivingProjectMoreThanOnce() throws Exception {
        AuthenticatedUser owner = createAuthenticatedUser(
                "Project Owner",
                "owner@example.com"
        );

        Project project = createProject(
                owner.user(),
                "Archived Project"
        );

        String authorization = "Bearer " + owner.accessToken();

        mockMvc.perform(delete("/api/v1/projects/{projectId}", project.getId())
                        .header(HttpHeaders.AUTHORIZATION, authorization))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/api/v1/projects/{projectId}", project.getId())
                        .header(HttpHeaders.AUTHORIZATION, authorization))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldRejectUnauthenticatedArchiveRequest() throws Exception {
        AuthenticatedUser owner = createAuthenticatedUser(
                "Project Owner",
                "owner@example.com"
        );

        Project project = createProject(
                owner.user(),
                "Protected Project"
        );

        mockMvc.perform(delete(
                        "/api/v1/projects/{projectId}",
                        project.getId()
                ))
                .andExpect(status().isUnauthorized());

        Project persistedProject = projectRepository
                .findById(project.getId())
                .orElseThrow();

        assertThat(persistedProject.getStatus())
                .isEqualTo(ProjectStatus.ACTIVE);
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

    private Project createProject(User owner, String name) {
        Project project = Project.create(
                owner,
                name,
                "Project used during automated tests"
        );

        return projectRepository.saveAndFlush(project);
    }

    private record AuthenticatedUser(
            User user,
            String accessToken
    ) {
    }
}