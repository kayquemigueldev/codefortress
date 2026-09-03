package com.codefortress.analysis.listing;

import com.codefortress.analysis.Analysis;
import com.codefortress.analysis.AnalysisSourceType;
import com.codefortress.analysis.AnalysisStatus;

import java.time.Instant;
import java.util.UUID;

public record ListedAnalysis(
        UUID id,
        int sequenceNumber,
        AnalysisStatus status,
        AnalysisSourceType sourceType,
        String sourceFilename,
        Short securityScore,
        Integer filesScanned,
        Long linesScanned,
        Integer findingsCount,
        Instant startedAt,
        Instant completedAt,
        Instant createdAt
) {

    public static ListedAnalysis from(
            Analysis analysis
    ) {
        return new ListedAnalysis(
                analysis.getId(),
                analysis.getSequenceNumber(),
                analysis.getStatus(),
                analysis.getSourceType(),
                analysis.getSourceFilename(),
                analysis.getSecurityScore(),
                analysis.getFilesScanned(),
                analysis.getLinesScanned(),
                analysis.getFindingsCount(),
                analysis.getStartedAt(),
                analysis.getCompletedAt(),
                analysis.getCreatedAt()
        );
    }
}