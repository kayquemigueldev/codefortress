package com.codefortress.analysis.discovery;

import java.nio.file.Path;

public record DiscoveredSourceFile(
        Path absolutePath,
        String relativePath,
        long sizeBytes,
        SourceFileCategory category
) {
}