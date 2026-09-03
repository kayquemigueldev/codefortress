package com.codefortress.analysis.upload;

import java.util.UUID;

public record StoredSourceArchive(
        UUID analysisId,
        String storageKey,
        long sizeBytes
) {
}