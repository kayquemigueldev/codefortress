package com.codefortress.analysis.listing;

import com.codefortress.analysis.AnalysisRepository;
import com.codefortress.identity.user.CurrentUserService;
import com.codefortress.project.ProjectRepository;
import com.codefortress.project.ProjectStatus;
import com.codefortress.project.details.ProjectNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ListAnalysesService {

    private final AnalysisRepository analysisRepository;
    private final ProjectRepository projectRepository;
    private final CurrentUserService currentUserService;

    public ListAnalysesService(
            AnalysisRepository analysisRepository,
            ProjectRepository projectRepository,
            CurrentUserService currentUserService
    ) {
        this.analysisRepository = analysisRepository;
        this.projectRepository = projectRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional(readOnly = true)
    public List<ListedAnalysis> list(
            UUID ownerId,
            UUID projectId
    ) {
        currentUserService.get(ownerId);

        projectRepository
                .findByIdAndOwner_Id(projectId, ownerId)
                .filter(project ->
                        project.getStatus()
                                == ProjectStatus.ACTIVE
                )
                .orElseThrow(
                        ProjectNotFoundException::new
                );

        return analysisRepository
                .findAllByProject_IdOrderBySequenceNumberDesc(
                        projectId
                )
                .stream()
                .map(ListedAnalysis::from)
                .toList();
    }
}