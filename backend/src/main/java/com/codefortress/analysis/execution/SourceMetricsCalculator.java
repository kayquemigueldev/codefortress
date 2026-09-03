package com.codefortress.analysis.execution;

import com.codefortress.analysis.discovery.DiscoveredSourceFile;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Objects;

@Component
public class SourceMetricsCalculator {

    public SourceMetrics calculate(
            List<DiscoveredSourceFile> sourceFiles
    ) {
        Objects.requireNonNull(
                sourceFiles,
                "sourceFiles must not be null"
        );

        long linesScanned = 0;

        try {
            for (DiscoveredSourceFile sourceFile
                    : sourceFiles) {
                linesScanned = Math.addExact(
                        linesScanned,
                        countLines(sourceFile)
                );
            }
        } catch (IOException | ArithmeticException exception) {
            throw new SourceMetricsCalculationException(
                    "SOURCE_METRICS_CALCULATION_FAILED",
                    "The source metrics could not be calculated",
                    exception
            );
        }

        return new SourceMetrics(
                sourceFiles.size(),
                linesScanned
        );
    }

    private long countLines(
            DiscoveredSourceFile sourceFile
    ) throws IOException {
        try (
                BufferedReader reader =
                        Files.newBufferedReader(
                                sourceFile.absolutePath(),
                                StandardCharsets.UTF_8
                        )
        ) {
            return reader.lines().count();
        }
    }
}