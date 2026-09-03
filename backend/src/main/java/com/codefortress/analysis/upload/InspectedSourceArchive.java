package com.codefortress.analysis.upload;

public record InspectedSourceArchive(
        String sourceFilename,
        String sourceReference,
        long sizeBytes
) {
}