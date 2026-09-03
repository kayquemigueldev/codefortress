package com.codefortress.analysis.upload;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import java.util.UUID;

@Component
public class LocalSourceArchiveStorage {

    private static final String STORED_FILENAME = "source.zip";

    private final Path storageRoot;

    public LocalSourceArchiveStorage(
            @Value(
                    "${ANALYSIS_STORAGE_PATH:"
                            + ".codefortress/storage}"
            )
            String storagePath
    ) {
        if (storagePath == null || storagePath.isBlank()) {
            throw new IllegalArgumentException(
                    "storagePath must not be blank"
            );
        }

        this.storageRoot = Path
                .of(storagePath)
                .toAbsolutePath()
                .normalize();
    }

    public StoredSourceArchive store(
            UUID analysisId,
            MultipartFile file
    ) {
        Objects.requireNonNull(
                analysisId,
                "analysisId must not be null"
        );

        if (file == null || file.isEmpty()) {
            throw failure(
                    "SOURCE_ARCHIVE_REQUIRED",
                    "A non-empty source archive is required"
            );
        }

        Path analysisDirectory =
                analysisDirectory(analysisId);

        Path destination =
                archiveLocation(analysisId);

        Path temporaryFile = analysisDirectory.resolve(
                ".upload-" + UUID.randomUUID() + ".tmp"
        );

        try {
            Files.createDirectories(analysisDirectory);

            if (Files.exists(destination)) {
                throw failure(
                        "SOURCE_ARCHIVE_ALREADY_STORED",
                        "A source archive is already stored "
                                + "for this analysis"
                );
            }

            try (
                    InputStream inputStream =
                            file.getInputStream();

                    OutputStream outputStream =
                            Files.newOutputStream(
                                    temporaryFile,
                                    StandardOpenOption.CREATE_NEW,
                                    StandardOpenOption.WRITE
                            )
            ) {
                inputStream.transferTo(outputStream);
            }

            moveAtomically(
                    temporaryFile,
                    destination
            );

            return new StoredSourceArchive(
                    analysisId,
                    storageKey(analysisId),
                    Files.size(destination)
            );
        } catch (SourceArchiveStorageException exception) {
            deleteTemporaryFile(temporaryFile);
            throw exception;
        } catch (IOException exception) {
            deleteTemporaryFile(temporaryFile);

            throw new SourceArchiveStorageException(
                    "SOURCE_ARCHIVE_STORAGE_FAILED",
                    "The source archive could not be stored",
                    exception
            );
        }
    }

    public InputStream open(UUID analysisId) {
        Objects.requireNonNull(
                analysisId,
                "analysisId must not be null"
        );

        Path archive = archiveLocation(analysisId);

        try {
            return Files.newInputStream(
                    archive,
                    StandardOpenOption.READ
            );
        } catch (IOException exception) {
            throw new SourceArchiveStorageException(
                    "SOURCE_ARCHIVE_NOT_FOUND",
                    "The source archive could not be found",
                    exception
            );
        }
    }

    public void delete(UUID analysisId) {
        Objects.requireNonNull(
                analysisId,
                "analysisId must not be null"
        );

        try {
            Files.deleteIfExists(
                    archiveLocation(analysisId)
            );
        } catch (IOException exception) {
            throw new SourceArchiveStorageException(
                    "SOURCE_ARCHIVE_DELETE_FAILED",
                    "The source archive could not be deleted",
                    exception
            );
        }
    }

    private void moveAtomically(
            Path source,
            Path destination
    ) throws IOException {
        try {
            Files.move(
                    source,
                    destination,
                    StandardCopyOption.ATOMIC_MOVE
            );
        } catch (AtomicMoveNotSupportedException exception) {
            Files.move(source, destination);
        }
    }

    private Path analysisDirectory(UUID analysisId) {
        Path directory = storageRoot
                .resolve("analyses")
                .resolve(analysisId.toString())
                .normalize();

        ensureInsideStorage(directory);

        return directory;
    }

    private Path archiveLocation(UUID analysisId) {
        Path archive = analysisDirectory(analysisId)
                .resolve(STORED_FILENAME)
                .normalize();

        ensureInsideStorage(archive);

        return archive;
    }

    private String storageKey(UUID analysisId) {
        return "analyses/"
                + analysisId
                + "/"
                + STORED_FILENAME;
    }

    private void ensureInsideStorage(Path path) {
        if (!path.startsWith(storageRoot)) {
            throw failure(
                    "INVALID_STORAGE_LOCATION",
                    "The source archive storage location is invalid"
            );
        }
    }

    private void deleteTemporaryFile(Path temporaryFile) {
        try {
            Files.deleteIfExists(temporaryFile);
        } catch (IOException ignored) {
            // A limpeza definitiva poderá ser feita por manutenção.
        }
    }

    private SourceArchiveStorageException failure(
            String code,
            String message
    ) {
        return new SourceArchiveStorageException(
                code,
                message
        );
    }
}