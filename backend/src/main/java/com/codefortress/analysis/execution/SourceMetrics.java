package com.codefortress.analysis.execution;

public record SourceMetrics(
        int filesScanned,
        long linesScanned
) {
}