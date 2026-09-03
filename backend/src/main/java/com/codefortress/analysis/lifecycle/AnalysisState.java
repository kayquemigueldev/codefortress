package com.codefortress.analysis.lifecycle;

import com.codefortress.analysis.Analysis;
import com.codefortress.analysis.AnalysisStatus;

import java.time.Instant;
import java.util.UUID;

public record AnalysisState(
        UUID id,
        AnalysisStatus status,
        Short securityScore,
        Integer filesScanned,
        Long linesScanned,
        Integer findingsCount,
        Instant startedAt,
        Instant completedAt,
        String failureCode,
        String failureMessage
) {

    public static AnalysisState from(Analysis analysis) {
        return new AnalysisState(
                analysis.getId(),
                analysis.getStatus(),
                analysis.getSecurityScore(),
                analysis.getFilesScanned(),
                analysis.getLinesScanned(),
                analysis.getFindingsCount(),
                analysis.getStartedAt(),
                analysis.getCompletedAt(),
                analysis.getFailureCode(),
                analysis.getFailureMessage()
        );
    }
}