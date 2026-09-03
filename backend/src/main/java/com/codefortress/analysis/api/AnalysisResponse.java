package com.codefortress.analysis.api;

import com.codefortress.analysis.AnalysisSourceType;
import com.codefortress.analysis.AnalysisStatus;
import com.codefortress.analysis.upload.UploadedAnalysis;

import java.time.Instant;
import java.util.UUID;

public record AnalysisResponse(
        UUID id,
        UUID projectId,
        int sequenceNumber,
        AnalysisStatus status,
        AnalysisSourceType sourceType,
        String sourceFilename,
        long sourceSizeBytes,
        Instant createdAt
) {

    public static AnalysisResponse from(
            UploadedAnalysis analysis
    ) {
        return new AnalysisResponse(
                analysis.id(),
                analysis.projectId(),
                analysis.sequenceNumber(),
                analysis.status(),
                analysis.sourceType(),
                analysis.sourceFilename(),
                analysis.sourceSizeBytes(),
                analysis.createdAt()
        );
    }
}