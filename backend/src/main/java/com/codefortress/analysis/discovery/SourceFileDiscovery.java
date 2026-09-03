package com.codefortress.analysis.discovery;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

@Component
public class SourceFileDiscovery {

    private static final int BINARY_SAMPLE_SIZE = 8_192;

    private static final Set<String> IGNORED_DIRECTORIES =
            Set.of(
                    ".git",
                    ".svn",
                    ".hg",
                    ".idea",
                    ".vscode",
                    ".gradle",
                    ".next",
                    ".nuxt",
                    "node_modules",
                    "target",
                    "build",
                    "dist",
                    "out",
                    "coverage",
                    "vendor"
            );

    private static final Set<String> DEPENDENCY_FILES =
            Set.of(
                    "pom.xml",
                    "package.json",
                    "package-lock.json",
                    "yarn.lock",
                    "pnpm-lock.yaml",
                    "build.gradle",
                    "build.gradle.kts",
                    "settings.gradle",
                    "settings.gradle.kts"
            );

    private static final Set<String> SOURCE_EXTENSIONS =
            Set.of(
                    ".java",
                    ".kt",
                    ".kts",
                    ".js",
                    ".jsx",
                    ".ts",
                    ".tsx",
                    ".py",
                    ".go",
                    ".cs",
                    ".php",
                    ".rb",
                    ".sql",
                    ".html",
                    ".css",
                    ".scss"
            );

    private static final Set<String> CONFIGURATION_EXTENSIONS =
            Set.of(
                    ".properties",
                    ".yml",
                    ".yaml",
                    ".json",
                    ".xml",
                    ".env",
                    ".conf",
                    ".config",
                    ".toml"
            );

    private final long maxScannableFileBytes;

    public SourceFileDiscovery(
            @Value(
                    "${ANALYSIS_MAX_SCANNABLE_FILE_SIZE:1MB}"
            )
            DataSize maxScannableFileSize
    ) {
        if (maxScannableFileSize == null
                || maxScannableFileSize.toBytes() < 1) {
            throw new IllegalArgumentException(
                    "maxScannableFileSize must be "
                            + "greater than zero"
            );
        }

        this.maxScannableFileBytes =
                maxScannableFileSize.toBytes();
    }

    public List<DiscoveredSourceFile> discover(
            Path workspace
    ) {
        if (workspace == null) {
            throw failure(
                    "SOURCE_WORKSPACE_REQUIRED",
                    "The source workspace is required"
            );
        }

        Path normalizedWorkspace = workspace
                .toAbsolutePath()
                .normalize();

        if (!Files.isDirectory(normalizedWorkspace)) {
            throw failure(
                    "SOURCE_WORKSPACE_NOT_FOUND",
                    "The source workspace could not be found"
            );
        }

        List<DiscoveredSourceFile> discoveredFiles =
                new ArrayList<>();

        try {
            Files.walkFileTree(
                    normalizedWorkspace,
                    new SimpleFileVisitor<>() {

                        @Override
                        public FileVisitResult preVisitDirectory(
                                Path directory,
                                BasicFileAttributes attributes
                        ) {
                            if (!directory.equals(normalizedWorkspace)
                                    && isIgnoredDirectory(
                                    directory
                            )) {
                                return FileVisitResult.SKIP_SUBTREE;
                            }

                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult visitFile(
                                Path file,
                                BasicFileAttributes attributes
                        ) throws IOException {
                            if (!attributes.isRegularFile()) {
                                return FileVisitResult.CONTINUE;
                            }

                            Optional<SourceFileCategory> category =
                                    classify(file);

                            if (category.isEmpty()) {
                                return FileVisitResult.CONTINUE;
                            }

                            if (attributes.size()
                                    > maxScannableFileBytes) {
                                return FileVisitResult.CONTINUE;
                            }

                            if (isBinary(file)) {
                                return FileVisitResult.CONTINUE;
                            }

                            String relativePath =
                                    normalizedWorkspace
                                            .relativize(file)
                                            .toString()
                                            .replace('\\', '/');

                            discoveredFiles.add(
                                    new DiscoveredSourceFile(
                                            file.toAbsolutePath()
                                                    .normalize(),
                                            relativePath,
                                            attributes.size(),
                                            category.orElseThrow()
                                    )
                            );

                            return FileVisitResult.CONTINUE;
                        }
                    }
            );
        } catch (IOException exception) {
            throw new SourceFileDiscoveryException(
                    "SOURCE_FILE_DISCOVERY_FAILED",
                    "The source files could not be discovered",
                    exception
            );
        }

        discoveredFiles.sort(
                Comparator.comparing(
                        DiscoveredSourceFile::relativePath
                )
        );

        return List.copyOf(discoveredFiles);
    }

    private Optional<SourceFileCategory> classify(
            Path file
    ) {
        String filename = file
                .getFileName()
                .toString()
                .toLowerCase(Locale.ROOT);

        if (DEPENDENCY_FILES.contains(filename)) {
            return Optional.of(
                    SourceFileCategory.DEPENDENCY_MANIFEST
            );
        }

        if (filename.equals("dockerfile")
                || filename.equals("makefile")
                || filename.equals(".env")) {
            return Optional.of(
                    SourceFileCategory.CONFIGURATION
            );
        }

        if (hasExtension(
                filename,
                CONFIGURATION_EXTENSIONS
        )) {
            return Optional.of(
                    SourceFileCategory.CONFIGURATION
            );
        }

        if (hasExtension(
                filename,
                SOURCE_EXTENSIONS
        )) {
            return Optional.of(
                    SourceFileCategory.SOURCE_CODE
            );
        }

        return Optional.empty();
    }

    private boolean hasExtension(
            String filename,
            Set<String> extensions
    ) {
        return extensions.stream()
                .anyMatch(filename::endsWith);
    }

    private boolean isIgnoredDirectory(Path directory) {
        String directoryName = directory
                .getFileName()
                .toString()
                .toLowerCase(Locale.ROOT);

        return IGNORED_DIRECTORIES.contains(
                directoryName
        );
    }

    private boolean isBinary(Path file)
            throws IOException {
        try (InputStream input = Files.newInputStream(file)) {
            byte[] sample =
                    input.readNBytes(BINARY_SAMPLE_SIZE);

            for (byte value : sample) {
                if (value == 0) {
                    return true;
                }
            }

            return false;
        }
    }

    private SourceFileDiscoveryException failure(
            String code,
            String message
    ) {
        return new SourceFileDiscoveryException(
                code,
                message
        );
    }
}