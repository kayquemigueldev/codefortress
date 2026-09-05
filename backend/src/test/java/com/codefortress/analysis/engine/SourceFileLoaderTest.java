package com.codefortress.analysis.engine;

import com.codefortress.analysis.discovery.DiscoveredSourceFile;
import com.codefortress.analysis.discovery.SourceFileCategory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SourceFileLoaderTest {

    @TempDir
    private Path workspace;

    private final SourceFileLoader loader =
            new SourceFileLoader();

    @Test
    void shouldLoadDiscoveredSourceFile()
            throws IOException {
        Path file = workspace.resolve(
                "src/Main.java"
        );

        Files.createDirectories(
                file.getParent()
        );

        Files.writeString(
                file,
                """
                class Main {
                    String message = "hello";
                }
                """,
                StandardCharsets.UTF_8
        );

        DiscoveredSourceFile discoveredFile =
                new DiscoveredSourceFile(
                        file,
                        "src/Main.java",
                        Files.size(file),
                        SourceFileCategory.SOURCE_CODE
                );

        ScannableFile scannableFile =
                loader.load(discoveredFile);

        assertThat(
                scannableFile.normalizedPath()
        ).isEqualTo(
                "src/Main.java"
        );

        assertThat(
                scannableFile.category()
        ).isEqualTo(
                SourceFileCategory.SOURCE_CODE
        );

        assertThat(
                scannableFile.content()
        ).contains(
                "String message = \"hello\";"
        );

        assertThat(
                scannableFile.lineCount()
        ).isEqualTo(3);
    }

    @Test
    void shouldLoadEmptyFile()
            throws IOException {
        Path file = workspace.resolve(
                "application.yml"
        );

        Files.writeString(
                file,
                "",
                StandardCharsets.UTF_8
        );

        DiscoveredSourceFile discoveredFile =
                new DiscoveredSourceFile(
                        file,
                        "application.yml",
                        Files.size(file),
                        SourceFileCategory.CONFIGURATION
                );

        ScannableFile scannableFile =
                loader.load(discoveredFile);

        assertThat(
                scannableFile.content()
        ).isEmpty();

        assertThat(
                scannableFile.lineCount()
        ).isZero();
    }

    @Test
    void shouldFailSafelyWhenFileCannotBeRead() {
        Path missingFile =
                workspace.resolve("missing.java");

        DiscoveredSourceFile discoveredFile =
                new DiscoveredSourceFile(
                        missingFile,
                        "missing.java",
                        10,
                        SourceFileCategory.SOURCE_CODE
                );

        assertThatThrownBy(() ->
                loader.load(discoveredFile)
        )
                .isInstanceOf(
                        SourceFileLoadingException.class
                )
                .extracting("code")
                .isEqualTo(
                        "SOURCE_FILE_LOADING_FAILED"
                );
    }
}