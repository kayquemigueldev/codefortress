package com.codefortress.analysis.api;

import com.codefortress.analysis.Analysis;
import com.codefortress.analysis.AnalysisRepository;
import com.codefortress.analysis.AnalysisStatus;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AnalysisControllerTest {

    private static final String PASSWORD =
            "correct-password";

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
    void shouldUploadSourceArchiveAndQueueAnalysis()
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

        byte[] zipContent = createZip(
                "src/Main.java",
                "class Main {}"
        );

        MockMultipartFile file =
                multipartFile(
                        "Meu Projeto.zip",
                        zipContent
                );

        mockMvc.perform(
                        multipart(
                                "/api/v1/projects/{projectId}/analyses",
                                project.getId()
                        )
                                .file(file)
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        bearer(owner)
                                )
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.projectId")
                        .value(project.getId().toString()))
                .andExpect(jsonPath("$.sequenceNumber")
                        .value(1))
                .andExpect(jsonPath("$.status")
                        .value("QUEUED"))
                .andExpect(jsonPath("$.sourceType")
                        .value("UPLOAD"))
                .andExpect(jsonPath("$.sourceFilename")
                        .value("Meu_Projeto.zip"))
                .andExpect(jsonPath("$.sourceSizeBytes")
                        .value(zipContent.length))
                .andExpect(jsonPath("$.createdAt")
                        .isNotEmpty());

        Analysis analysis = analysisRepository
                .findTopByProject_IdOrderBySequenceNumberDesc(
                        project.getId()
                )
                .orElseThrow();

        assertThat(analysis.getStatus())
                .isEqualTo(AnalysisStatus.QUEUED);
        assertThat(analysis.getSourceReference())
                .matches("[a-f0-9]{64}");
    }

    @Test
    void shouldRejectInvalidSourceArchive()
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

        MockMultipartFile invalidFile =
                multipartFile(
                        "fake.zip",
                        "not a zip".getBytes(
                                StandardCharsets.UTF_8
                        )
                );

        mockMvc.perform(
                        multipart(
                                "/api/v1/projects/{projectId}/analyses",
                                project.getId()
                        )
                                .file(invalidFile)
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        bearer(owner)
                                )
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code")
                        .value("INVALID_SOURCE_ARCHIVE"));

        assertThat(
                analysisRepository
                        .findAllByProject_IdOrderBySequenceNumberDesc(
                                project.getId()
                        )
        ).isEmpty();
    }

    @Test
    void shouldHideProjectOwnedByAnotherUser()
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

        mockMvc.perform(
                        multipart(
                                "/api/v1/projects/{projectId}/analyses",
                                project.getId()
                        )
                                .file(
                                        multipartFile(
                                                "source.zip",
                                                createZip(
                                                        "README.md",
                                                        "# Private"
                                                )
                                        )
                                )
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        bearer(anotherUser)
                                )
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code")
                        .value("PROJECT_NOT_FOUND"));

        assertThat(
                analysisRepository
                        .findAllByProject_IdOrderBySequenceNumberDesc(
                                project.getId()
                        )
        ).isEmpty();
    }

    @Test
    void shouldRejectUnauthenticatedUpload()
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
                        multipart(
                                "/api/v1/projects/{projectId}/analyses",
                                project.getId()
                        )
                                .file(
                                        multipartFile(
                                                "source.zip",
                                                createZip(
                                                        "README.md",
                                                        "# Protected"
                                                )
                                        )
                                )
                )
                .andExpect(status().isUnauthorized());

        assertThat(
                analysisRepository
                        .findAllByProject_IdOrderBySequenceNumberDesc(
                                project.getId()
                        )
        ).isEmpty();
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

    private MockMultipartFile multipartFile(
            String filename,
            byte[] content
    ) {
        return new MockMultipartFile(
                "file",
                filename,
                "application/zip",
                content
        );
    }

    private byte[] createZip(
            String entryName,
            String content
    ) throws IOException {
        ByteArrayOutputStream bytes =
                new ByteArrayOutputStream();

        try (ZipOutputStream zip =
                     new ZipOutputStream(bytes)) {
            zip.putNextEntry(new ZipEntry(entryName));
            zip.write(
                    content.getBytes(StandardCharsets.UTF_8)
            );
            zip.closeEntry();
        }

        return bytes.toByteArray();
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