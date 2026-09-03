package com.codefortress.analysis.extraction;

import com.codefortress.analysis.upload.LocalSourceArchiveStorage;
import com.codefortress.analysis.upload.SourceArchiveStorageException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Comparator;
import java.util.Objects;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.stream.Stream;

@Component
public class SourceArchiveExtractor {

    private static final int BUFFER_SIZE = 8_192;

    private final LocalSourceArchiveStorage archiveStorage;
    private final Path workspaceRoot;
    private final long maxExtractedBytes;
    private final long maxFileBytes;
    private final int maxEntries;

    public SourceArchiveExtractor(
            LocalSourceArchiveStorage archiveStorage,

            @Value(
                    "${ANALYSIS_WORKSPACE_PATH:"
                            + ".codefortress/workspaces}"
            )
            String workspacePath,

            @Value(
                    "${ANALYSIS_MAX_EXTRACTED_SIZE:50MB}"
            )
            DataSize maxExtractedSize,

            @Value(
                    "${ANALYSIS_MAX_EXTRACTED_FILE_SIZE:5MB}"
            )
            DataSize maxFileSize,

            @Value(
                    "${ANALYSIS_MAX_ARCHIVE_ENTRIES:5000}"
            )
            int maxEntries
    ) {
        this.archiveStorage = Objects.requireNonNull(
                archiveStorage,
                "archiveStorage must not be null"
        );

        if (workspacePath == null
                || workspacePath.isBlank()) {
            throw new IllegalArgumentException(
                    "workspacePath must not be blank"
            );
        }

        if (maxExtractedSize == null
                || maxExtractedSize.toBytes() < 1) {
            throw new IllegalArgumentException(
                    "maxExtractedSize must be greater than zero"
            );
        }

        if (maxFileSize == null
                || maxFileSize.toBytes() < 1) {
            throw new IllegalArgumentException(
                    "maxFileSize must be greater than zero"
            );
        }

        if (maxEntries < 1) {
            throw new IllegalArgumentException(
                    "maxEntries must be greater than zero"
            );
        }

        this.workspaceRoot = Path
                .of(workspacePath)
                .toAbsolutePath()
                .normalize();

        this.maxExtractedBytes =
                maxExtractedSize.toBytes();

        this.maxFileBytes =
                maxFileSize.toBytes();

        this.maxEntries = maxEntries;
    }

    public ExtractedSourceArchive extract(
            UUID analysisId
    ) {
        Objects.requireNonNull(
                analysisId,
                "analysisId must not be null"
        );

        Path workspace = workspace(analysisId);
        boolean workspaceCreated = false;

        try {
            Files.createDirectories(
                    workspace.getParent()
            );

            Files.createDirectory(workspace);
            workspaceCreated = true;

            ExtractionCounters counters =
                    extractEntries(
                            analysisId,
                            workspace
                    );

            if (counters.filesExtracted() == 0) {
                throw failure(
                        "SOURCE_ARCHIVE_EMPTY",
                        "The source archive contains no files"
                );
            }

            return new ExtractedSourceArchive(
                    analysisId,
                    workspace,
                    counters.filesExtracted(),
                    counters.bytesExtracted()
            );
        } catch (
                SourceArchiveExtractionException exception
        ) {
            if (workspaceCreated) {
                deleteWorkspaceQuietly(workspace);
            }

            throw exception;
        } catch (
                IOException
                | SourceArchiveStorageException exception
        ) {
            if (workspaceCreated) {
                deleteWorkspaceQuietly(workspace);
            }

            throw new SourceArchiveExtractionException(
                    "SOURCE_ARCHIVE_EXTRACTION_FAILED",
                    "The source archive could not be extracted",
                    exception
            );
        }
    }

    public void deleteWorkspace(UUID analysisId) {
        Objects.requireNonNull(
                analysisId,
                "analysisId must not be null"
        );

        Path workspace = workspace(analysisId);

        try {
            deleteWorkspaceContents(workspace);
        } catch (IOException exception) {
            throw new SourceArchiveExtractionException(
                    "SOURCE_WORKSPACE_DELETE_FAILED",
                    "The source workspace could not be deleted",
                    exception
            );
        }
    }

    private ExtractionCounters extractEntries(
            UUID analysisId,
            Path workspace
    ) throws IOException {
        int entries = 0;
        int filesExtracted = 0;
        long bytesExtracted = 0;

        try (
                InputStream archiveInput =
                        archiveStorage.open(analysisId);

                ZipInputStream zipInput =
                        new ZipInputStream(archiveInput)
        ) {
            ZipEntry entry;

            while ((entry = zipInput.getNextEntry())
                    != null) {
                entries++;

                if (entries > maxEntries) {
                    throw failure(
                            "SOURCE_ARCHIVE_TOO_MANY_ENTRIES",
                            "The source archive contains "
                                    + "too many entries"
                    );
                }

                Path target = resolveEntry(
                        workspace,
                        entry.getName()
                );

                if (entry.isDirectory()) {
                    Files.createDirectories(target);
                    zipInput.closeEntry();
                    continue;
                }

                Files.createDirectories(
                        target.getParent()
                );

                long fileBytes = 0;

                try (
                        OutputStream output =
                                Files.newOutputStream(
                                        target,
                                        StandardOpenOption.CREATE_NEW,
                                        StandardOpenOption.WRITE
                                )
                ) {
                    byte[] buffer =
                            new byte[BUFFER_SIZE];

                    int bytesRead;

                    while ((bytesRead =
                            zipInput.read(buffer)) != -1) {
                        if (bytesRead == 0) {
                            continue;
                        }

                        fileBytes += bytesRead;

                        if (fileBytes > maxFileBytes) {
                            throw failure(
                                    "SOURCE_FILE_TOO_LARGE",
                                    "A source file exceeds "
                                            + "the allowed size"
                            );
                        }

                        bytesExtracted += bytesRead;

                        if (bytesExtracted
                                > maxExtractedBytes) {
                            throw failure(
                                    "SOURCE_ARCHIVE_EXPANDED_TOO_LARGE",
                                    "The extracted source archive "
                                            + "exceeds the allowed size"
                            );
                        }

                        output.write(
                                buffer,
                                0,
                                bytesRead
                        );
                    }
                }

                filesExtracted++;
                zipInput.closeEntry();
            }
        }

        return new ExtractionCounters(
                filesExtracted,
                bytesExtracted
        );
    }

    private Path resolveEntry(
            Path workspace,
            String entryName
    ) {
        if (entryName == null
                || entryName.isBlank()) {
            throw failure(
                    "INVALID_SOURCE_ARCHIVE_ENTRY",
                    "The source archive contains "
                            + "an invalid entry"
            );
        }

        String normalizedName =
                entryName.replace('\\', '/');

        if (normalizedName.startsWith("/")
                || normalizedName.matches(
                "^[A-Za-z]:/.*"
        )) {
            throw zipSlip();
        }

        try {
            Path target = workspace
                    .resolve(normalizedName)
                    .normalize();

            if (!target.startsWith(workspace)
                    || target.equals(workspace)) {
                throw zipSlip();
            }

            return target;
        } catch (InvalidPathException exception) {
            throw new SourceArchiveExtractionException(
                    "INVALID_SOURCE_ARCHIVE_ENTRY",
                    "The source archive contains "
                            + "an invalid entry",
                    exception
            );
        }
    }

    private Path workspace(UUID analysisId) {
        Path workspace = workspaceRoot
                .resolve("analyses")
                .resolve(analysisId.toString())
                .normalize();

        if (!workspace.startsWith(workspaceRoot)) {
            throw failure(
                    "INVALID_SOURCE_WORKSPACE",
                    "The source workspace is invalid"
            );
        }

        return workspace;
    }

    private void deleteWorkspaceQuietly(
            Path workspace
    ) {
        try {
            deleteWorkspaceContents(workspace);
        } catch (IOException ignored) {
            // Uma rotina de manutenção poderá
            // remover workspaces órfãos.
        }
    }

    private void deleteWorkspaceContents(
            Path workspace
    ) throws IOException {
        if (!Files.exists(workspace)) {
            return;
        }

        if (!workspace.startsWith(workspaceRoot)) {
            throw new IOException(
                    "Workspace is outside the configured root"
            );
        }

        try (Stream<Path> paths =
                     Files.walk(workspace)) {
            for (Path path : paths
                    .sorted(Comparator.reverseOrder())
                    .toList()) {
                Files.deleteIfExists(path);
            }
        }
    }

    private SourceArchiveExtractionException zipSlip() {
        return failure(
                "UNSAFE_SOURCE_ARCHIVE_ENTRY",
                "The source archive contains "
                        + "an unsafe entry path"
        );
    }

    private SourceArchiveExtractionException failure(
            String code,
            String message
    ) {
        return new SourceArchiveExtractionException(
                code,
                message
        );
    }

    private record ExtractionCounters(
            int filesExtracted,
            long bytesExtracted
    ) {
    }
}