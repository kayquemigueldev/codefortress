package com.codefortress.analysis.execution;

import com.codefortress.analysis.Analysis;
import com.codefortress.analysis.AnalysisRepository;
import com.codefortress.analysis.AnalysisStatus;
import com.codefortress.analysis.lifecycle.AnalysisState;
import com.codefortress.analysis.upload.LocalSourceArchiveStorage;
import com.codefortress.analysis.upload.SourceArchiveStorageException;
import com.codefortress.analysis.upload.UploadAnalysisService;
import com.codefortress.analysis.upload.UploadedAnalysis;
import com.codefortress.identity.user.User;
import com.codefortress.identity.user.UserRepository;
import com.codefortress.project.Project;
import com.codefortress.project.ProjectRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class AnalysisExecutionServiceTest {

    @Autowired
    private AnalysisExecutionService executionService;

    @Autowired
    private UploadAnalysisService uploadAnalysisService;

    @Autowired
    private LocalSourceArchiveStorage archiveStorage;

    @Autowired
    private AnalysisRepository analysisRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldExecuteAnalysisAndPersistMetrics()
            throws IOException {
        User owner = createUser();
        Project project = createProject(owner);

        Map<String, String> entries =
                new LinkedHashMap<>();

        entries.put(
                "src/Main.java",
                "line one\nline two\n"
        );

        entries.put(
                "application.yml",
                "debug: true\n"
        );

        entries.put(
                "README.md",
                "# Ignored documentation\n"
        );

        UploadedAnalysis uploaded =
                uploadAnalysisService.upload(
                        owner.getId(),
                        project.getId(),
                        multipartFile(
                                createZip(entries)
                        )
                );

        AnalysisState result =
                executionService.execute(
                        uploaded.id()
                );

        assertThat(result.status())
                .isEqualTo(AnalysisStatus.COMPLETED);
        assertThat(result.securityScore())
                .isEqualTo((short) 100);
        assertThat(result.filesScanned())
                .isEqualTo(2);
        assertThat(result.linesScanned())
                .isEqualTo(3L);
        assertThat(result.findingsCount())
                .isZero();
        assertThat(result.startedAt()).isNotNull();
        assertThat(result.completedAt()).isNotNull();

        Analysis persisted = analysisRepository
                .findById(uploaded.id())
                .orElseThrow();

        assertThat(persisted.getStatus())
                .isEqualTo(AnalysisStatus.COMPLETED);

        assertThatThrownBy(() ->
                archiveStorage.open(uploaded.id())
        )
                .isInstanceOf(
                        SourceArchiveStorageException.class
                )
                .extracting("code")
                .isEqualTo("SOURCE_ARCHIVE_NOT_FOUND");
    }

    @Test
    void shouldCountDetectedSecurityFindings()
            throws IOException {
        User owner = createUser();
        Project project = createProject(owner);

        Map<String, String> entries =
                new LinkedHashMap<>();

        entries.put(
                "src/Config.java",
                """
                class Config {
                    String password = "super-secret-password";
                }
                """
        );

        UploadedAnalysis uploaded =
                uploadAnalysisService.upload(
                        owner.getId(),
                        project.getId(),
                        multipartFile(
                                createZip(entries)
                        )
                );

        AnalysisState result =
                executionService.execute(
                        uploaded.id()
                );

        assertThat(result.status())
                .isEqualTo(
                        AnalysisStatus.COMPLETED
                );

        assertThat(result.filesScanned())
                .isEqualTo(1);

        assertThat(result.linesScanned())
                .isEqualTo(3L);

        assertThat(result.findingsCount())
                .isEqualTo(1);

        assertThat(result.securityScore())
                .isEqualTo((short) 100);
    }

    @Test
    void shouldFailAnalysisWhenArchiveHasNoFiles()
            throws IOException {
        User owner = createUser();
        Project project = createProject(owner);

        UploadedAnalysis uploaded =
                uploadAnalysisService.upload(
                        owner.getId(),
                        project.getId(),
                        multipartFile(
                                createZip(Map.of())
                        )
                );

        AnalysisState result =
                executionService.execute(
                        uploaded.id()
                );

        assertThat(result.status())
                .isEqualTo(AnalysisStatus.FAILED);
        assertThat(result.failureCode())
                .isEqualTo("SOURCE_ARCHIVE_EMPTY");
        assertThat(result.failureMessage())
                .isEqualTo(
                        "The source archive contains no files"
                );
        assertThat(result.completedAt()).isNotNull();

        assertThatThrownBy(() ->
                archiveStorage.open(uploaded.id())
        )
                .isInstanceOf(
                        SourceArchiveStorageException.class
                );
    }

    private User createUser() {
        User user = User.create(
                "owner@example.com",
                "test-password-hash",
                "Project Owner"
        );

        return userRepository.saveAndFlush(user);
    }

    private Project createProject(User owner) {
        Project project = Project.create(
                owner,
                "CodeFortress API",
                "Project used during automated tests"
        );

        return projectRepository.saveAndFlush(project);
    }

    private MockMultipartFile multipartFile(
            byte[] content
    ) {
        return new MockMultipartFile(
                "file",
                "source.zip",
                "application/zip",
                content
        );
    }

    private byte[] createZip(
            Map<String, String> entries
    ) throws IOException {
        ByteArrayOutputStream bytes =
                new ByteArrayOutputStream();

        try (ZipOutputStream zip =
                     new ZipOutputStream(bytes)) {
            for (Map.Entry<String, String> entry
                    : entries.entrySet()) {
                zip.putNextEntry(
                        new ZipEntry(entry.getKey())
                );

                zip.write(
                        entry.getValue().getBytes(
                                StandardCharsets.UTF_8
                        )
                );

                zip.closeEntry();
            }
        }

        return bytes.toByteArray();
    }
}