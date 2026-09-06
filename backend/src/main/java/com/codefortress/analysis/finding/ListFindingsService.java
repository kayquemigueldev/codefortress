package com.codefortress.analysis.finding;

import com.codefortress.analysis.AnalysisRepository;
import com.codefortress.analysis.FindingRepository;
import com.codefortress.analysis.lifecycle.AnalysisNotFoundException;
import com.codefortress.identity.user.CurrentUserService;
import com.codefortress.project.ProjectRepository;
import com.codefortress.project.ProjectStatus;
import com.codefortress.project.details.ProjectNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ListFindingsService {

    private final AnalysisRepository analysisRepository;
    private final FindingRepository findingRepository;
    private final ProjectRepository projectRepository;
    private final CurrentUserService currentUserService;

    public ListFindingsService(
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

    @Transactional(readOnly = true)
    public List<ListedFinding> list(
            UUID ownerId,
            UUID projectId,
            UUID analysisId
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

        return findingRepository
                .findAllByAnalysis_IdOrderByCreatedAtAsc(
                        analysisId
                )
                .stream()
                .map(ListedFinding::from)
                .toList();
    }
}