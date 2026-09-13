package com.codefortress.dashboard.api;

import com.codefortress.analysis.AnalysisStatus;
import com.codefortress.dashboard.DashboardOverview;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record DashboardResponse(
        long activeProjects,
        Integer averageSecurityScore,
        long openFindings,
        long criticalOpenFindings,
        long highOpenFindings,
        long mediumOpenFindings,
        long lowOpenFindings,
        LatestAnalysisResponse latestAnalysis,
        List<LatestAnalysisResponse> recentAnalyses
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

        List<LatestAnalysisResponse> recent =
                overview.recentAnalyses()
                        .stream()
                        .map(
                                LatestAnalysisResponse::from
                        )
                        .toList();

        return new DashboardResponse(
                overview.activeProjects(),
                overview.averageSecurityScore(),
                overview.openFindings(),
                overview.criticalOpenFindings(),
                overview.highOpenFindings(),
                overview.mediumOpenFindings(),
                overview.lowOpenFindings(),
                latest,
                recent
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