package com.codefortress.analysis.api;

import com.codefortress.analysis.Analysis;
import com.codefortress.analysis.AnalysisRepository;
import com.codefortress.analysis.AnalysisStatus;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AnalysisControllerTest {

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

    @Test
    void shouldListFindingsForAnalysis()
            throws Exception {
        AuthenticatedUser owner =
                createAuthenticatedUser(
                        "Project Owner",
                        "owner@example.com"
                );

        Project project =
                createProject(
                        owner.user(),
                        "CodeFortress API"
                );

        Analysis analysis =
                createAnalysis(
                        project,
                        1
                );

        Finding finding =
                createFinding(
                        analysis
                );

        mockMvc.perform(
                        get(
                                "/api/v1/projects/{projectId}/analyses/{analysisId}/findings",
                                project.getId(),
                                analysis.getId()
                        )
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        bearer(owner)
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()")
                        .value(1))
                .andExpect(jsonPath("$[0].id")
                        .value(
                                finding.getId()
                                        .toString()
                        ))
                .andExpect(jsonPath("$[0].ruleKey")
                        .value("CF-SEC-001"))
                .andExpect(jsonPath("$[0].ruleVersion")
                        .value("1.0.0"))
                .andExpect(jsonPath("$[0].title")
                        .value("Hardcoded Secret"))
                .andExpect(jsonPath("$[0].category")
                        .value("SECRETS"))
                .andExpect(jsonPath("$[0].severity")
                        .value("CRITICAL"))
                .andExpect(jsonPath("$[0].status")
                        .value("OPEN"))
                .andExpect(jsonPath("$[0].filePath")
                        .value("src/Config.java"))
                .andExpect(jsonPath("$[0].startLine")
                        .value(2))
                .andExpect(jsonPath("$[0].endLine")
                        .value(2))
                .andExpect(jsonPath("$[0].codeExcerpt")
                        .value(
                                "String password = \"********\";"
                        ))
                .andExpect(jsonPath("$[0].description")
                        .isNotEmpty())
                .andExpect(jsonPath("$[0].impact")
                        .isNotEmpty())
                .andExpect(jsonPath("$[0].recommendation")
                        .isNotEmpty())
                .andExpect(jsonPath("$[0].fingerprint")
                        .value("b".repeat(64)))
                .andExpect(jsonPath("$[0].createdAt")
                        .isNotEmpty())
                .andExpect(jsonPath("$[0].statusUpdatedAt")
                        .isNotEmpty());
    }

    @Test
    void shouldHideAnalysisFromAnotherProject()
            throws Exception {
        AuthenticatedUser owner =
                createAuthenticatedUser(
                        "Project Owner",
                        "owner@example.com"
                );

        Project firstProject =
                createProject(
                        owner.user(),
                        "First Project"
                );

        Project secondProject =
                createProject(
                        owner.user(),
                        "Second Project"
                );

        Analysis analysis =
                createAnalysis(
                        secondProject,
                        1
                );

        mockMvc.perform(
                        get(
                                "/api/v1/projects/{projectId}/analyses/{analysisId}/findings",
                                firstProject.getId(),
                                analysis.getId()
                        )
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        bearer(owner)
                                )
                )
                .andExpect(
                        status().isNotFound()
                )
                .andExpect(
                        jsonPath("$.code")
                                .value("ANALYSIS_NOT_FOUND")
                );
    }

    @Test
    void shouldRejectUnauthenticatedFindingsRequest()
            throws Exception {
        User owner =
                createUser(
                        "owner@example.com",
                        "Project Owner"
                );

        Project project =
                createProject(
                        owner,
                        "Protected Project"
                );

        Analysis analysis =
                createAnalysis(
                        project,
                        1
                );

        mockMvc.perform(
                        get(
                                "/api/v1/projects/{projectId}/analyses/{analysisId}/findings",
                                project.getId(),
                                analysis.getId()
                        )
                )
                .andExpect(
                        status().isUnauthorized()
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

    private Finding createFinding(
            Analysis analysis
    ) {
        Finding finding =
                Finding.create(
                        analysis,
                        "CF-SEC-001",
                        "1.0.0",
                        "b".repeat(64),
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