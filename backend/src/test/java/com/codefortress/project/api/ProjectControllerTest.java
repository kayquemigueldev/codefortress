package com.codefortress.project.api;

import com.codefortress.identity.authentication.LoginCommand;
import com.codefortress.identity.authentication.LoginService;
import com.codefortress.identity.authentication.LoginResult;
import com.codefortress.identity.registration.RegisterUserCommand;
import com.codefortress.identity.registration.RegisterUserService;
import com.codefortress.project.creation.CreateProjectCommand;
import com.codefortress.project.creation.CreateProjectService;
import com.codefortress.project.creation.CreatedProject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RegisterUserService registerUserService;

    @Autowired
    private LoginService loginService;

    @Autowired
    private CreateProjectService createProjectService;

    @Test
    void shouldCreateProjectForAuthenticatedUser() throws Exception {
        String accessToken = registerAndLogin();

        mockMvc.perform(post("/api/v1/projects")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer " + accessToken
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "  CodeFortress API  ",
                                  "description": "  Security analysis backend  "
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name")
                        .value("CodeFortress API"))
                .andExpect(jsonPath("$.description")
                        .value("Security analysis backend"))
                .andExpect(jsonPath("$.status")
                        .value("ACTIVE"))
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.updatedAt").isNotEmpty());
    }

    @Test
    void shouldListProjectsFromAuthenticatedUserNewestFirst()
            throws Exception {
        String accessToken = registerAndLogin();

        createProject(accessToken, "First Project");
        createProject(accessToken, "Second Project");

        mockMvc.perform(get("/api/v1/projects")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer " + accessToken
                        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name")
                        .value("Second Project"))
                .andExpect(jsonPath("$[0].status")
                        .value("ACTIVE"))
                .andExpect(jsonPath("$[1].name")
                        .value("First Project"))
                .andExpect(jsonPath("$[1].status")
                        .value("ACTIVE"));
    }

    @Test
    void shouldGetProjectBelongingToAuthenticatedUser()
            throws Exception {
        LoginResult loginResult = registerAndLoginWithResult();

        CreatedProject project = createProjectService.create(
                new CreateProjectCommand(
                        loginResult.userId(),
                        "CodeFortress",
                        "Security analysis platform"
                )
        );

        mockMvc.perform(get(
                        "/api/v1/projects/{projectId}",
                        project.id()
                )
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer "
                                        + loginResult.accessToken().value()
                        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(project.id().toString()))
                .andExpect(jsonPath("$.name")
                        .value("CodeFortress"))
                .andExpect(jsonPath("$.description")
                        .value("Security analysis platform"))
                .andExpect(jsonPath("$.status")
                        .value("ACTIVE"));
    }

    @Test
    void shouldReturnNotFoundForUnknownProject()
            throws Exception {
        String accessToken = registerAndLogin();
        UUID unknownProjectId = UUID.randomUUID();

        mockMvc.perform(get(
                        "/api/v1/projects/{projectId}",
                        unknownProjectId
                )
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer " + accessToken
                        ))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code")
                        .value("PROJECT_NOT_FOUND"))
                .andExpect(jsonPath("$.message")
                        .value("Project not found"));
    }

    @Test
    void shouldRequireAuthenticationToGetProject()
            throws Exception {
        mockMvc.perform(get(
                        "/api/v1/projects/{projectId}",
                        UUID.randomUUID()
                ))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldUpdateProjectBelongingToAuthenticatedUser()
            throws Exception {
        LoginResult loginResult = registerAndLoginWithResult();

        CreatedProject project = createProjectService.create(
                new CreateProjectCommand(
                        loginResult.userId(),
                        "Old Name",
                        "Old description"
                )
        );

        mockMvc.perform(put(
                        "/api/v1/projects/{projectId}",
                        project.id()
                )
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer "
                                        + loginResult.accessToken().value()
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "name": "  New Name  ",
                              "description": "  New description  "
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(project.id().toString()))
                .andExpect(jsonPath("$.name")
                        .value("New Name"))
                .andExpect(jsonPath("$.description")
                        .value("New description"))
                .andExpect(jsonPath("$.status")
                        .value("ACTIVE"));
    }

    @Test
    void shouldHideProjectWhenUpdatedByAnotherUser()
            throws Exception {
        LoginResult ownerLogin = registerAndLoginWithResult();

        CreatedProject project = createProjectService.create(
                new CreateProjectCommand(
                        ownerLogin.userId(),
                        "Private Project",
                        null
                )
        );

        registerUserService.register(
                new RegisterUserCommand(
                        "Another User",
                        "another@example.com",
                        "another-password"
                )
        );

        LoginResult anotherLogin = loginService.login(
                new LoginCommand(
                        "another@example.com",
                        "another-password"
                )
        );

        mockMvc.perform(put(
                        "/api/v1/projects/{projectId}",
                        project.id()
                )
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer "
                                        + anotherLogin.accessToken().value()
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "name": "Stolen Project",
                              "description": null
                            }
                            """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code")
                        .value("PROJECT_NOT_FOUND"));
    }

    @Test
    void shouldRejectInvalidProjectUpdate()
            throws Exception {
        LoginResult loginResult = registerAndLoginWithResult();

        CreatedProject project = createProjectService.create(
                new CreateProjectCommand(
                        loginResult.userId(),
                        "CodeFortress",
                        null
                )
        );

        mockMvc.perform(put(
                        "/api/v1/projects/{projectId}",
                        project.id()
                )
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer "
                                        + loginResult.accessToken().value()
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "name": "   ",
                              "description": "Invalid update"
                            }
                            """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code")
                        .value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors.name").exists());
    }

    @Test
    void shouldRequireAuthenticationToUpdateProject()
            throws Exception {
        mockMvc.perform(put(
                        "/api/v1/projects/{projectId}",
                        UUID.randomUUID()
                )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "name": "Updated Project",
                              "description": null
                            }
                            """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectDuplicatedActiveProjectName() throws Exception {
        String accessToken = registerAndLogin();

        createProject(accessToken, "CodeFortress");

        mockMvc.perform(post("/api/v1/projects")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer " + accessToken
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "  codefortress  ",
                                  "description": "Duplicated project"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code")
                        .value("PROJECT_NAME_ALREADY_EXISTS"))
                .andExpect(jsonPath("$.message")
                        .value(
                                "An active project with this name already exists"
                        ));
    }

    @Test
    void shouldRejectInvalidProjectRequest() throws Exception {
        String accessToken = registerAndLogin();

        mockMvc.perform(post("/api/v1/projects")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer " + accessToken
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "   ",
                                  "description": "Invalid project"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code")
                        .value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors.name").exists());
    }

    @Test
    void shouldRequireAuthenticationToCreateProject()
            throws Exception {
        mockMvc.perform(post("/api/v1/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "CodeFortress",
                                  "description": "Security platform"
                                }
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRequireAuthenticationToListProjects()
            throws Exception {
        mockMvc.perform(get("/api/v1/projects"))
                .andExpect(status().isUnauthorized());
    }

    private void createProject(
            String accessToken,
            String name
    ) throws Exception {
        mockMvc.perform(post("/api/v1/projects")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer " + accessToken
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "%s"
                                }
                                """.formatted(name)))
                .andExpect(status().isCreated());
    }

    private String registerAndLogin() {
        return registerAndLoginWithResult()
                .accessToken()
                .value();
    }

    private LoginResult registerAndLoginWithResult() {
        registerUserService.register(
                new RegisterUserCommand(
                        "Kayque Miguel",
                        "kayque@example.com",
                        "correct-password"
                )
        );

        return loginService.login(
                new LoginCommand(
                        "kayque@example.com",
                        "correct-password"
                )
        );
    }
}