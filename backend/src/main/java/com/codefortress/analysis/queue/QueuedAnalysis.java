package com.codefortress.analysis.queue;

import com.codefortress.analysis.Analysis;
import com.codefortress.analysis.AnalysisSourceType;
import com.codefortress.analysis.AnalysisStatus;

import java.time.Instant;
import java.util.UUID;

public record QueuedAnalysis(
        UUID id,
        UUID projectId,
        int sequenceNumber,
        AnalysisStatus status,
        AnalysisSourceType sourceType,
        String sourceFilename,
        Instant createdAt
) {

    public static QueuedAnalysis from(Analysis analysis) {
        return new QueuedAnalysis(
                analysis.getId(),
                analysis.getProject().getId(),
                analysis.getSequenceNumber(),
                analysis.getStatus(),
                analysis.getSourceType(),
                analysis.getSourceFilename(),
                analysis.getCreatedAt()
        );
    }
}