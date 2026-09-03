package com.codefortress.analysis.discovery;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.util.unit.DataSize;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.groups.Tuple.tuple;

class SourceFileDiscoveryTest {

    @TempDir
    private Path workspace;

    @Test
    void shouldDiscoverAndClassifyRelevantFiles()
            throws IOException {
        write(
                "src/Main.java",
                "class Main {}"
        );

        write(
                "src/app.ts",
                "const app = true;"
        );

        write(
                "application.yml",
                "server:\n  port: 8080"
        );

        write(
                "pom.xml",
                "<project></project>"
        );

        write(
                "README.md",
                "# Documentation"
        );

        SourceFileDiscovery discovery =
                createDiscovery(
                        DataSize.ofMegabytes(1)
                );

        assertThat(discovery.discover(workspace))
                .extracting(
                        DiscoveredSourceFile::relativePath,
                        DiscoveredSourceFile::category
                )
                .containsExactly(
                        tuple(
                                "application.yml",
                                SourceFileCategory.CONFIGURATION
                        ),
                        tuple(
                                "pom.xml",
                                SourceFileCategory
                                        .DEPENDENCY_MANIFEST
                        ),
                        tuple(
                                "src/Main.java",
                                SourceFileCategory.SOURCE_CODE
                        ),
                        tuple(
                                "src/app.ts",
                                SourceFileCategory.SOURCE_CODE
                        )
                );
    }

    @Test
    void shouldIgnoreGeneratedAndDependencyDirectories()
            throws IOException {
        write(
                "src/App.ts",
                "export const app = true;"
        );

        write(
                "node_modules/package/index.js",
                "ignored"
        );

        write(
                "target/Generated.java",
                "class Generated {}"
        );

        write(
                ".git/config",
                "ignored"
        );

        SourceFileDiscovery discovery =
                createDiscovery(
                        DataSize.ofMegabytes(1)
                );

        assertThat(discovery.discover(workspace))
                .extracting(
                        DiscoveredSourceFile::relativePath
                )
                .containsExactly("src/App.ts");
    }

    @Test
    void shouldIgnoreBinaryAndOversizedFiles()
            throws IOException {
        write(
                "valid.js",
                "ok"
        );

        Path binaryFile =
                workspace.resolve("binary.java");

        Files.write(
                binaryFile,
                new byte[]{
                        0x01,
                        0x00,
                        0x02
                }
        );

        write(
                "large.java",
                "12345"
        );

        SourceFileDiscovery discovery =
                createDiscovery(
                        DataSize.ofBytes(4)
                );

        assertThat(discovery.discover(workspace))
                .extracting(
                        DiscoveredSourceFile::relativePath
                )
                .containsExactly("valid.js");
    }

    @Test
    void shouldRejectMissingWorkspace() {
        SourceFileDiscovery discovery =
                createDiscovery(
                        DataSize.ofMegabytes(1)
                );

        Path missingWorkspace =
                workspace.resolve("missing");

        assertThatThrownBy(() ->
                discovery.discover(missingWorkspace)
        )
                .isInstanceOf(
                        SourceFileDiscoveryException.class
                )
                .extracting("code")
                .isEqualTo("SOURCE_WORKSPACE_NOT_FOUND");
    }

    private SourceFileDiscovery createDiscovery(
            DataSize maxFileSize
    ) {
        return new SourceFileDiscovery(maxFileSize);
    }

    private void write(
            String relativePath,
            String content
    ) throws IOException {
        Path file = workspace.resolve(relativePath);

        Files.createDirectories(file.getParent());

        Files.writeString(
                file,
                content,
                StandardCharsets.UTF_8
        );
    }
}