package com.codefortress.analysis.api;

import com.codefortress.analysis.AnalysisSourceType;
import com.codefortress.analysis.AnalysisStatus;
import com.codefortress.analysis.listing.ListedAnalysis;

import java.time.Instant;
import java.util.UUID;

public record AnalysisHistoryResponse(
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

    public static AnalysisHistoryResponse from(
            ListedAnalysis analysis
    ) {
        return new AnalysisHistoryResponse(
                analysis.id(),
                analysis.sequenceNumber(),
                analysis.status(),
                analysis.sourceType(),
                analysis.sourceFilename(),
                analysis.securityScore(),
                analysis.filesScanned(),
                analysis.linesScanned(),
                analysis.findingsCount(),
                analysis.startedAt(),
                analysis.completedAt(),
                analysis.createdAt()
        );
    }
}