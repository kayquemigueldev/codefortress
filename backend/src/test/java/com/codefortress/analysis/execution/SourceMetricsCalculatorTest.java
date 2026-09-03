package com.codefortress.analysis.execution;

import com.codefortress.analysis.discovery.DiscoveredSourceFile;
import com.codefortress.analysis.discovery.SourceFileCategory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SourceMetricsCalculatorTest {

    @TempDir
    private Path workspace;

    private final SourceMetricsCalculator calculator =
            new SourceMetricsCalculator();

    @Test
    void shouldCountFilesAndLines()
            throws IOException {
        DiscoveredSourceFile javaFile = createFile(
                "Main.java",
                "line one\nline two\nline three"
        );

        DiscoveredSourceFile emptyFile = createFile(
                "application.yml",
                ""
        );

        SourceMetrics metrics = calculator.calculate(
                List.of(javaFile, emptyFile)
        );

        assertThat(metrics.filesScanned()).isEqualTo(2);
        assertThat(metrics.linesScanned()).isEqualTo(3);
    }

    @Test
    void shouldReturnZeroForEmptyFileList() {
        SourceMetrics metrics =
                calculator.calculate(List.of());

        assertThat(metrics.filesScanned()).isZero();
        assertThat(metrics.linesScanned()).isZero();
    }

    @Test
    void shouldFailSafelyWhenFileCannotBeRead() {
        Path missingFile =
                workspace.resolve("missing.java");

        DiscoveredSourceFile sourceFile =
                new DiscoveredSourceFile(
                        missingFile,
                        "missing.java",
                        10,
                        SourceFileCategory.SOURCE_CODE
                );

        assertThatThrownBy(() ->
                calculator.calculate(
                        List.of(sourceFile)
                )
        )
                .isInstanceOf(
                        SourceMetricsCalculationException.class
                )
                .extracting("code")
                .isEqualTo(
                        "SOURCE_METRICS_CALCULATION_FAILED"
                );
    }

    private DiscoveredSourceFile createFile(
            String filename,
            String content
    ) throws IOException {
        Path file = workspace.resolve(filename);

        Files.writeString(
                file,
                content,
                StandardCharsets.UTF_8
        );

        return new DiscoveredSourceFile(
                file,
                filename,
                Files.size(file),
                SourceFileCategory.SOURCE_CODE
        );
    }
}