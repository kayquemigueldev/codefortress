package com.codefortress.analysis.upload;

import com.codefortress.analysis.AnalysisSourceType;
import com.codefortress.analysis.AnalysisStatus;
import com.codefortress.analysis.queue.QueuedAnalysis;

import java.time.Instant;
import java.util.UUID;

public record UploadedAnalysis(
        UUID id,
        UUID projectId,
        int sequenceNumber,
        AnalysisStatus status,
        AnalysisSourceType sourceType,
        String sourceFilename,
        long sourceSizeBytes,
        Instant createdAt
) {

    public static UploadedAnalysis from(
            QueuedAnalysis queuedAnalysis,
            InspectedSourceArchive inspectedArchive
    ) {
        return new UploadedAnalysis(
                queuedAnalysis.id(),
                queuedAnalysis.projectId(),
                queuedAnalysis.sequenceNumber(),
                queuedAnalysis.status(),
                queuedAnalysis.sourceType(),
                inspectedArchive.sourceFilename(),
                inspectedArchive.sizeBytes(),
                queuedAnalysis.createdAt()
        );
    }
}