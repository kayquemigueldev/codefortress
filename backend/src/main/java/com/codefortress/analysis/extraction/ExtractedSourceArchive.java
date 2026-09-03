package com.codefortress.analysis.extraction;

import java.nio.file.Path;
import java.util.UUID;

public record ExtractedSourceArchive(
        UUID analysisId,
        Path workspacePath,
        int filesExtracted,
        long bytesExtracted
) {
}