package com.codefortress.analysis.upload;

import com.codefortress.analysis.Analysis;
import com.codefortress.analysis.AnalysisRepository;
import com.codefortress.analysis.AnalysisStatus;
import com.codefortress.identity.user.User;
import com.codefortress.identity.user.UserRepository;
import com.codefortress.project.Project;
import com.codefortress.project.ProjectRepository;
import com.codefortress.project.details.ProjectNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class UploadAnalysisServiceTest {

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
    void shouldInspectQueueAndStoreSourceArchive()
            throws IOException {
        User owner = createUser(
                "owner@example.com",
                "Project Owner"
        );

        Project project = createProject(
                owner,
                "CodeFortress API"
        );

        byte[] zipContent = createZip(
                "src/Main.java",
                "class Main {}"
        );

        UploadedAnalysis uploadedAnalysis =
                uploadAnalysisService.upload(
                        owner.getId(),
                        project.getId(),
                        multipartFile(
                                "Meu Projeto.zip",
                                zipContent
                        )
                );

        Analysis persistedAnalysis = analysisRepository
                .findById(uploadedAnalysis.id())
                .orElseThrow();

        assertThat(uploadedAnalysis.projectId())
                .isEqualTo(project.getId());
        assertThat(uploadedAnalysis.sequenceNumber())
                .isEqualTo(1);
        assertThat(uploadedAnalysis.status())
                .isEqualTo(AnalysisStatus.QUEUED);
        assertThat(uploadedAnalysis.sourceFilename())
                .isEqualTo("Meu_Projeto.zip");
        assertThat(uploadedAnalysis.sourceSizeBytes())
                .isEqualTo(zipContent.length);
        assertThat(uploadedAnalysis.createdAt()).isNotNull();

        assertThat(persistedAnalysis.getSourceReference())
                .matches("[a-f0-9]{64}");
        assertThat(persistedAnalysis.getSourceFilename())
                .isEqualTo("Meu_Projeto.zip");

        try (InputStream storedArchive =
                     archiveStorage.open(
                             uploadedAnalysis.id()
                     )) {
            assertThat(storedArchive.readAllBytes())
                    .containsExactly(zipContent);
        }
    }

    @Test
    void shouldRejectInvalidArchiveBeforeCreatingAnalysis() {
        User owner = createUser(
                "owner@example.com",
                "Project Owner"
        );

        Project project = createProject(
                owner,
                "CodeFortress API"
        );

        MockMultipartFile invalidFile =
                new MockMultipartFile(
                        "file",
                        "fake.zip",
                        "application/zip",
                        "not a zip".getBytes(
                                StandardCharsets.UTF_8
                        )
                );

        assertThatThrownBy(() ->
                uploadAnalysisService.upload(
                        owner.getId(),
                        project.getId(),
                        invalidFile
                )
        ).isInstanceOf(
                InvalidSourceArchiveException.class
        );

        assertThat(
                analysisRepository
                        .findAllByProject_IdOrderBySequenceNumberDesc(
                                project.getId()
                        )
        ).isEmpty();
    }

    @Test
    void shouldRejectUploadForArchivedProject()
            throws IOException {
        User owner = createUser(
                "owner@example.com",
                "Project Owner"
        );

        Project project = createProject(
                owner,
                "Archived Project"
        );

        project.archive();
        projectRepository.saveAndFlush(project);

        assertThatThrownBy(() ->
                uploadAnalysisService.upload(
                        owner.getId(),
                        project.getId(),
                        multipartFile(
                                "source.zip",
                                createZip(
                                        "README.md",
                                        "# Archived project"
                                )
                        )
                )
        ).isInstanceOf(ProjectNotFoundException.class);

        assertThat(
                analysisRepository
                        .findAllByProject_IdOrderBySequenceNumberDesc(
                                project.getId()
                        )
        ).isEmpty();
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
}