package com.codefortress.analysis.extraction;

import com.codefortress.analysis.upload.LocalSourceArchiveStorage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.unit.DataSize;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SourceArchiveExtractorTest {

    @TempDir
    private Path temporaryDirectory;

    @Test
    void shouldExtractSourceArchiveSafely()
            throws IOException {
        UUID analysisId = UUID.randomUUID();

        LocalSourceArchiveStorage storage =
                createStorage();

        storage.store(
                analysisId,
                multipartFile(
                        createZip(
                                Map.of(
                                        "src/Main.java",
                                        "class Main {}",
                                        "README.md",
                                        "# Project"
                                )
                        )
                )
        );

        SourceArchiveExtractor extractor =
                createExtractor(storage);

        ExtractedSourceArchive result =
                extractor.extract(analysisId);

        assertThat(result.analysisId())
                .isEqualTo(analysisId);
        assertThat(result.filesExtracted())
                .isEqualTo(2);
        assertThat(result.bytesExtracted())
                .isGreaterThan(0);

        assertThat(
                result.workspacePath()
                        .resolve("src/Main.java")
        )
                .exists()
                .hasContent("class Main {}");

        assertThat(
                result.workspacePath()
                        .resolve("README.md")
        )
                .exists()
                .hasContent("# Project");
    }

    @Test
    void shouldRejectZipSlipEntry()
            throws IOException {
        UUID analysisId = UUID.randomUUID();

        LocalSourceArchiveStorage storage =
                createStorage();

        storage.store(
                analysisId,
                multipartFile(
                        createZip(
                                Map.of(
                                        "../../outside.txt",
                                        "unsafe"
                                )
                        )
                )
        );

        SourceArchiveExtractor extractor =
                createExtractor(storage);

        assertThatThrownBy(() ->
                extractor.extract(analysisId)
        )
                .isInstanceOf(
                        SourceArchiveExtractionException.class
                )
                .extracting("code")
                .isEqualTo(
                        "UNSAFE_SOURCE_ARCHIVE_ENTRY"
                );

        assertThat(
                temporaryDirectory
                        .resolve("workspaces")
                        .resolve("outside.txt")
        ).doesNotExist();
    }

    @Test
    void shouldRejectArchiveWithTooManyEntries()
            throws IOException {
        UUID analysisId = UUID.randomUUID();

        LocalSourceArchiveStorage storage =
                createStorage();

        storage.store(
                analysisId,
                multipartFile(
                        createZip(
                                Map.of(
                                        "first.txt",
                                        "first",
                                        "second.txt",
                                        "second"
                                )
                        )
                )
        );

        SourceArchiveExtractor extractor =
                new SourceArchiveExtractor(
                        storage,
                        workspacePath(),
                        DataSize.ofMegabytes(50),
                        DataSize.ofMegabytes(5),
                        1
                );

        assertThatThrownBy(() ->
                extractor.extract(analysisId)
        )
                .isInstanceOf(
                        SourceArchiveExtractionException.class
                )
                .extracting("code")
                .isEqualTo(
                        "SOURCE_ARCHIVE_TOO_MANY_ENTRIES"
                );
    }

    @Test
    void shouldRejectOversizedExtractedFile()
            throws IOException {
        UUID analysisId = UUID.randomUUID();

        LocalSourceArchiveStorage storage =
                createStorage();

        storage.store(
                analysisId,
                multipartFile(
                        createZip(
                                Map.of(
                                        "large.txt",
                                        "12345"
                                )
                        )
                )
        );

        SourceArchiveExtractor extractor =
                new SourceArchiveExtractor(
                        storage,
                        workspacePath(),
                        DataSize.ofBytes(10),
                        DataSize.ofBytes(4),
                        10
                );

        assertThatThrownBy(() ->
                extractor.extract(analysisId)
        )
                .isInstanceOf(
                        SourceArchiveExtractionException.class
                )
                .extracting("code")
                .isEqualTo("SOURCE_FILE_TOO_LARGE");
    }

    @Test
    void shouldRejectArchiveWithoutFiles()
            throws IOException {
        UUID analysisId = UUID.randomUUID();

        LocalSourceArchiveStorage storage =
                createStorage();

        storage.store(
                analysisId,
                multipartFile(createZip(Map.of()))
        );

        SourceArchiveExtractor extractor =
                createExtractor(storage);

        assertThatThrownBy(() ->
                extractor.extract(analysisId)
        )
                .isInstanceOf(
                        SourceArchiveExtractionException.class
                )
                .extracting("code")
                .isEqualTo("SOURCE_ARCHIVE_EMPTY");
    }

    private LocalSourceArchiveStorage createStorage() {
        return new LocalSourceArchiveStorage(
                temporaryDirectory
                        .resolve("storage")
                        .toString()
        );
    }

    private SourceArchiveExtractor createExtractor(
            LocalSourceArchiveStorage storage
    ) {
        return new SourceArchiveExtractor(
                storage,
                workspacePath(),
                DataSize.ofMegabytes(50),
                DataSize.ofMegabytes(5),
                5_000
        );
    }

    private String workspacePath() {
        return temporaryDirectory
                .resolve("workspaces")
                .toString();
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