package com.codefortress.project.details;

import com.codefortress.identity.user.CurrentUserService;
import com.codefortress.project.Project;
import com.codefortress.project.ProjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class GetProjectService {

    private final ProjectRepository projectRepository;
    private final CurrentUserService currentUserService;

    public GetProjectService(
            ProjectRepository projectRepository,
            CurrentUserService currentUserService
    ) {
        this.projectRepository = projectRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional(readOnly = true)
    public ProjectDetails get(
            UUID ownerId,
            UUID projectId
    ) {
        currentUserService.get(ownerId);

        Project project = projectRepository
                .findByIdAndOwner_Id(
                        projectId,
                        ownerId
                )
                .orElseThrow(ProjectNotFoundException::new);

        return ProjectDetails.from(project);
    }
}