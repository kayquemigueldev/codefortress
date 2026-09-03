package com.codefortress.analysis.upload;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LocalSourceArchiveStorageTest {

    @TempDir
    private Path temporaryDirectory;

    @Test
    void shouldStoreAndOpenArchive() throws IOException {
        LocalSourceArchiveStorage storage =
                createStorage();

        UUID analysisId = UUID.randomUUID();

        byte[] content = new byte[]{
                0x50,
                0x4B,
                0x03,
                0x04,
                0x01
        };

        StoredSourceArchive storedArchive = storage.store(
                analysisId,
                multipartFile(content)
        );

        assertThat(storedArchive.analysisId())
                .isEqualTo(analysisId);
        assertThat(storedArchive.storageKey())
                .isEqualTo(
                        "analyses/"
                                + analysisId
                                + "/source.zip"
                );
        assertThat(storedArchive.sizeBytes())
                .isEqualTo(content.length);

        try (InputStream inputStream =
                     storage.open(analysisId)) {
            assertThat(inputStream.readAllBytes())
                    .containsExactly(content);
        }
    }

    @Test
    void shouldKeepAnalysesIsolated() throws IOException {
        LocalSourceArchiveStorage storage =
                createStorage();

        UUID firstAnalysisId = UUID.randomUUID();
        UUID secondAnalysisId = UUID.randomUUID();

        byte[] firstContent = new byte[]{
                0x50, 0x4B, 0x03, 0x04, 0x01
        };

        byte[] secondContent = new byte[]{
                0x50, 0x4B, 0x03, 0x04, 0x02
        };

        storage.store(
                firstAnalysisId,
                multipartFile(firstContent)
        );

        storage.store(
                secondAnalysisId,
                multipartFile(secondContent)
        );

        try (
                InputStream first =
                        storage.open(firstAnalysisId);

                InputStream second =
                        storage.open(secondAnalysisId)
        ) {
            assertThat(first.readAllBytes())
                    .containsExactly(firstContent);

            assertThat(second.readAllBytes())
                    .containsExactly(secondContent);
        }
    }

    @Test
    void shouldNotOverwriteExistingArchive() {
        LocalSourceArchiveStorage storage =
                createStorage();

        UUID analysisId = UUID.randomUUID();

        storage.store(
                analysisId,
                multipartFile(new byte[]{
                        0x50, 0x4B, 0x03, 0x04, 0x01
                })
        );

        assertThatThrownBy(() ->
                storage.store(
                        analysisId,
                        multipartFile(new byte[]{
                                0x50, 0x4B, 0x03, 0x04, 0x02
                        })
                )
        )
                .isInstanceOf(
                        SourceArchiveStorageException.class
                )
                .extracting("code")
                .isEqualTo(
                        "SOURCE_ARCHIVE_ALREADY_STORED"
                );
    }

    @Test
    void shouldDeleteStoredArchive() {
        LocalSourceArchiveStorage storage =
                createStorage();

        UUID analysisId = UUID.randomUUID();

        storage.store(
                analysisId,
                multipartFile(new byte[]{
                        0x50, 0x4B, 0x03, 0x04, 0x01
                })
        );

        storage.delete(analysisId);

        assertThatThrownBy(() ->
                storage.open(analysisId)
        )
                .isInstanceOf(
                        SourceArchiveStorageException.class
                )
                .extracting("code")
                .isEqualTo("SOURCE_ARCHIVE_NOT_FOUND");
    }

    private LocalSourceArchiveStorage createStorage() {
        return new LocalSourceArchiveStorage(
                temporaryDirectory.toString()
        );
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
}