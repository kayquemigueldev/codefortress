package com.codefortress.project.archiving;

import com.codefortress.identity.user.CurrentUserService;
import com.codefortress.project.Project;
import com.codefortress.project.ProjectRepository;
import com.codefortress.project.details.ProjectNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ArchiveProjectService {

    private final ProjectRepository projectRepository;
    private final CurrentUserService currentUserService;

    public ArchiveProjectService(
            ProjectRepository projectRepository,
            CurrentUserService currentUserService
    ) {
        this.projectRepository = projectRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public void archive(UUID ownerId, UUID projectId) {
        currentUserService.get(ownerId);

        Project project = projectRepository
                .findByIdAndOwner_Id(projectId, ownerId)
                .orElseThrow(ProjectNotFoundException::new);

        project.archive();

        projectRepository.saveAndFlush(project);
    }
}