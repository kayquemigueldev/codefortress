package com.codefortress.analysis.upload;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.unit.DataSize;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SourceArchiveInspectorTest {

    private final SourceArchiveInspector inspector =
            new SourceArchiveInspector(
                    DataSize.ofMegabytes(10)
            );

    @Test
    void shouldInspectValidZipArchive() throws IOException {
        byte[] content = createZip(
                "src/Main.java",
                "class Main {}"
        );

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "Meu Projeto.zip",
                "application/zip",
                content
        );

        InspectedSourceArchive result = inspector.inspect(file);

        assertThat(result.sourceFilename())
                .isEqualTo("Meu_Projeto.zip");
        assertThat(result.sourceReference())
                .matches("[a-f0-9]{64}");
        assertThat(result.sizeBytes())
                .isEqualTo(content.length);
    }

    @Test
    void shouldRemovePathFromFilename() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "../../private/project.zip",
                "application/zip",
                createZip("README.md", "# Project")
        );

        InspectedSourceArchive result = inspector.inspect(file);

        assertThat(result.sourceFilename())
                .isEqualTo("project.zip");
    }

    @Test
    void shouldRejectNonZipExtension() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "source.txt",
                "text/plain",
                createZip("source.txt", "content")
        );

        assertThatThrownBy(() -> inspector.inspect(file))
                .isInstanceOf(
                        InvalidSourceArchiveException.class
                )
                .extracting("code")
                .isEqualTo(
                        "INVALID_SOURCE_ARCHIVE_TYPE"
                );
    }

    @Test
    void shouldRejectContentWithoutZipSignature() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "fake.zip",
                "application/zip",
                "this is not a zip"
                        .getBytes(StandardCharsets.UTF_8)
        );

        assertThatThrownBy(() -> inspector.inspect(file))
                .isInstanceOf(
                        InvalidSourceArchiveException.class
                )
                .extracting("code")
                .isEqualTo("INVALID_SOURCE_ARCHIVE");
    }

    @Test
    void shouldRejectArchiveLargerThanLimit() {
        SourceArchiveInspector smallInspector =
                new SourceArchiveInspector(
                        DataSize.ofBytes(4)
                );

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "large.zip",
                "application/zip",
                new byte[]{
                        0x50,
                        0x4B,
                        0x03,
                        0x04,
                        0x01
                }
        );

        assertThatThrownBy(() ->
                smallInspector.inspect(file)
        )
                .isInstanceOf(
                        InvalidSourceArchiveException.class
                )
                .extracting("code")
                .isEqualTo("SOURCE_ARCHIVE_TOO_LARGE");
    }

    @Test
    void shouldRejectEmptyArchiveUpload() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "empty.zip",
                "application/zip",
                new byte[0]
        );

        assertThatThrownBy(() -> inspector.inspect(file))
                .isInstanceOf(
                        InvalidSourceArchiveException.class
                )
                .extracting("code")
                .isEqualTo("SOURCE_ARCHIVE_REQUIRED");
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
}