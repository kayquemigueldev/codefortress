package com.codefortress.analysis.queue;

import com.codefortress.analysis.Analysis;
import com.codefortress.analysis.AnalysisRepository;
import com.codefortress.identity.user.CurrentUserService;
import com.codefortress.project.Project;
import com.codefortress.project.ProjectRepository;
import com.codefortress.project.ProjectStatus;
import com.codefortress.project.details.ProjectNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class QueueAnalysisService {

    private static final String RULE_SET_VERSION = "rules-v1";
    private static final String SCORE_VERSION = "score-v1";

    private final AnalysisRepository analysisRepository;
    private final ProjectRepository projectRepository;
    private final CurrentUserService currentUserService;

    public QueueAnalysisService(
            AnalysisRepository analysisRepository,
            ProjectRepository projectRepository,
            CurrentUserService currentUserService
    ) {
        this.analysisRepository = analysisRepository;
        this.projectRepository = projectRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public QueuedAnalysis queue(
            UUID ownerId,
            UUID projectId,
            QueueAnalysisCommand command
    ) {
        currentUserService.get(ownerId);

        Project project = projectRepository
                .findOwnedByIdForUpdate(projectId, ownerId)
                .filter(foundProject ->
                        foundProject.getStatus() == ProjectStatus.ACTIVE
                )
                .orElseThrow(ProjectNotFoundException::new);

        int sequenceNumber = analysisRepository
                .findTopByProject_IdOrderBySequenceNumberDesc(projectId)
                .map(Analysis::getSequenceNumber)
                .map(previousSequence -> previousSequence + 1)
                .orElse(1);

        Analysis analysis = Analysis.queueUpload(
                project,
                sequenceNumber,
                command.sourceReference(),
                command.sourceFilename(),
                RULE_SET_VERSION,
                SCORE_VERSION
        );

        Analysis savedAnalysis =
                analysisRepository.saveAndFlush(analysis);

        return QueuedAnalysis.from(savedAnalysis);
    }
}