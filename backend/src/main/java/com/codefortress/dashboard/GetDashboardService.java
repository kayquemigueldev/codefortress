package com.codefortress.dashboard;

import com.codefortress.analysis.AnalysisRepository;
import com.codefortress.analysis.FindingRepository;
import com.codefortress.analysis.FindingStatus;
import com.codefortress.analysis.Severity;
import com.codefortress.identity.user.CurrentUserService;
import com.codefortress.project.ProjectRepository;
import com.codefortress.project.ProjectStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class GetDashboardService {

    private final ProjectRepository projectRepository;
    private final AnalysisRepository analysisRepository;
    private final FindingRepository findingRepository;
    private final CurrentUserService currentUserService;

    public GetDashboardService(
            ProjectRepository projectRepository,
            AnalysisRepository analysisRepository,
            FindingRepository findingRepository,
            CurrentUserService currentUserService
    ) {
        this.projectRepository = projectRepository;
        this.analysisRepository = analysisRepository;
        this.findingRepository = findingRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional(readOnly = true)
    public DashboardOverview get(UUID ownerId) {
        currentUserService.get(ownerId);

        long activeProjects =
                projectRepository.countByOwner_IdAndStatus(
                        ownerId,
                        ProjectStatus.ACTIVE
                );

        long openFindings =
                findingRepository
                        .countByAnalysis_Project_Owner_IdAndAnalysis_Project_StatusAndStatus(
                                ownerId,
                                ProjectStatus.ACTIVE,
                                FindingStatus.OPEN
                        );

        long criticalOpenFindings =
                findingRepository
                        .countByAnalysis_Project_Owner_IdAndAnalysis_Project_StatusAndStatusAndSeverity(
                                ownerId,
                                ProjectStatus.ACTIVE,
                                FindingStatus.OPEN,
                                Severity.CRITICAL
                        );

        DashboardOverview.LatestAnalysis latestAnalysis =
                analysisRepository
                        .findTopByProject_Owner_IdAndProject_StatusOrderByCreatedAtDesc(
                                ownerId,
                                ProjectStatus.ACTIVE
                        )
                        .map(DashboardOverview.LatestAnalysis::from)
                        .orElse(null);

        return new DashboardOverview(
                activeProjects,
                openFindings,
                criticalOpenFindings,
                latestAnalysis
        );
    }
}