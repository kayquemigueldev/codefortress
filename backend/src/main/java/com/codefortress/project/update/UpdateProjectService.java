package com.codefortress.project.update;

import com.codefortress.identity.user.CurrentUserService;
import com.codefortress.project.Project;
import com.codefortress.project.ProjectRepository;
import com.codefortress.project.ProjectStatus;
import com.codefortress.project.creation.ProjectNameAlreadyExistsException;
import com.codefortress.project.details.ProjectNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UpdateProjectService {

    private final ProjectRepository projectRepository;
    private final CurrentUserService currentUserService;

    public UpdateProjectService(
            ProjectRepository projectRepository,
            CurrentUserService currentUserService
    ) {
        this.projectRepository = projectRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public UpdatedProject update(
            UpdateProjectCommand command
    ) {
        currentUserService.get(command.ownerId());

        Project project = projectRepository
                .findByIdAndOwner_Id(
                        command.projectId(),
                        command.ownerId()
                )
                .filter(foundProject ->
                        foundProject.getStatus()
                                == ProjectStatus.ACTIVE
                )
                .orElseThrow(ProjectNotFoundException::new);

        String normalizedName = normalizeName(command.name());

        boolean duplicatedName = projectRepository
                .existsByOwner_IdAndNameIgnoreCaseAndStatusAndIdNot(
                        command.ownerId(),
                        normalizedName,
                        ProjectStatus.ACTIVE,
                        command.projectId()
                );

        if (duplicatedName) {
            throw new ProjectNameAlreadyExistsException();
        }

        project.updateDetails(
                normalizedName,
                command.description()
        );

        Project updatedProject =
                projectRepository.saveAndFlush(project);

        return UpdatedProject.from(updatedProject);
    }

    private String normalizeName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "name must not be blank"
            );
        }

        return name.trim();
    }
}