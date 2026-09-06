package com.codefortress.dashboard.api;

import com.codefortress.analysis.AnalysisStatus;
import com.codefortress.dashboard.DashboardOverview;

import java.time.Instant;
import java.util.UUID;

public record DashboardResponse(
        long activeProjects,
        long openFindings,
        long criticalOpenFindings,
        LatestAnalysisResponse latestAnalysis
) {

    public static DashboardResponse from(
            DashboardOverview overview
    ) {
        LatestAnalysisResponse latest =
                overview.latestAnalysis() == null
                        ? null
                        : LatestAnalysisResponse.from(
                        overview.latestAnalysis()
                );

        return new DashboardResponse(
                overview.activeProjects(),
                overview.openFindings(),
                overview.criticalOpenFindings(),
                latest
        );
    }

    public record LatestAnalysisResponse(
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

        public static LatestAnalysisResponse from(
                DashboardOverview.LatestAnalysis analysis
        ) {
            return new LatestAnalysisResponse(
                    analysis.id(),
                    analysis.projectId(),
                    analysis.projectName(),
                    analysis.sequenceNumber(),
                    analysis.status(),
                    analysis.sourceFilename(),
                    analysis.securityScore(),
                    analysis.findingsCount(),
                    analysis.createdAt(),
                    analysis.completedAt()
            );
        }
    }
}