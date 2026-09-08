package com.codefortress.analysis.finding;

import com.codefortress.analysis.AnalysisRepository;
import com.codefortress.analysis.Finding;
import com.codefortress.analysis.FindingRepository;
import com.codefortress.analysis.FindingStatus;
import com.codefortress.analysis.lifecycle.AnalysisNotFoundException;
import com.codefortress.identity.user.CurrentUserService;
import com.codefortress.project.ProjectRepository;
import com.codefortress.project.ProjectStatus;
import com.codefortress.project.details.ProjectNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UpdateFindingStatusService {

    private final AnalysisRepository analysisRepository;
    private final FindingRepository findingRepository;
    private final ProjectRepository projectRepository;
    private final CurrentUserService currentUserService;

    public UpdateFindingStatusService(
            AnalysisRepository analysisRepository,
            FindingRepository findingRepository,
            ProjectRepository projectRepository,
            CurrentUserService currentUserService
    ) {
        this.analysisRepository = analysisRepository;
        this.findingRepository = findingRepository;
        this.projectRepository = projectRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public ListedFinding update(
            UUID ownerId,
            UUID projectId,
            UUID analysisId,
            UUID findingId,
            FindingStatus status
    ) {
        currentUserService.get(ownerId);

        projectRepository
                .findByIdAndOwner_Id(
                        projectId,
                        ownerId
                )
                .filter(project ->
                        project.getStatus()
                                == ProjectStatus.ACTIVE
                )
                .orElseThrow(
                        ProjectNotFoundException::new
                );

        analysisRepository
                .findByIdAndProject_IdAndProject_Owner_Id(
                        analysisId,
                        projectId,
                        ownerId
                )
                .orElseThrow(
                        AnalysisNotFoundException::new
                );

        Finding finding =
                findingRepository
                        .findByIdAndAnalysis_Id(
                                findingId,
                                analysisId
                        )
                        .orElseThrow(
                                FindingNotFoundException::new
                        );

        finding.updateStatus(status);

        return ListedFinding.from(finding);
    }
}