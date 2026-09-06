package com.codefortress.dashboard;

import com.codefortress.analysis.Analysis;
import com.codefortress.analysis.AnalysisStatus;

import java.time.Instant;
import java.util.UUID;

public record DashboardOverview(
        long activeProjects,
        long openFindings,
        long criticalOpenFindings,
        LatestAnalysis latestAnalysis
) {

    public record LatestAnalysis(
            UUID id,
            UUID projectId,
            String projectName,
            int sequenceNumber,
            AnalysisStatus status,
            String sourceFilename,
            Short securityScore,
            Integer findingsCount,
            Instant createdAt,
            Instant completedAt
    ) {

        public static LatestAnalysis from(
                Analysis analysis
        ) {
            return new LatestAnalysis(
                    analysis.getId(),
                    analysis.getProject().getId(),
                    analysis.getProject().getName(),
                    analysis.getSequenceNumber(),
                    analysis.getStatus(),
                    analysis.getSourceFilename(),
                    analysis.getSecurityScore(),
                    analysis.getFindingsCount(),
                    analysis.getCreatedAt(),
                    analysis.getCompletedAt()
            );
        }
    }
}