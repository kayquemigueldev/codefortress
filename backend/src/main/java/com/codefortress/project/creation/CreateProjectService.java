package com.codefortress.project.creation;

import com.codefortress.identity.user.CurrentUserUnavailableException;
import com.codefortress.identity.user.User;
import com.codefortress.identity.user.UserRepository;
import com.codefortress.identity.user.UserStatus;
import com.codefortress.project.Project;
import com.codefortress.project.ProjectRepository;
import com.codefortress.project.ProjectStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public CreateProjectService(
            ProjectRepository projectRepository,
            UserRepository userRepository
    ) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public CreatedProject create(CreateProjectCommand command) {
        User owner = userRepository
                .findById(command.ownerId())
                .filter(user ->
                        user.getStatus() == UserStatus.ACTIVE
                )
                .orElseThrow(
                        CurrentUserUnavailableException::new
                );

        Project project = Project.create(
                owner,
                command.name(),
                command.description()
        );

        boolean duplicatedName = projectRepository
                .existsByOwner_IdAndNameIgnoreCaseAndStatus(
                        owner.getId(),
                        project.getName(),
                        ProjectStatus.ACTIVE
                );

        if (duplicatedName) {
            throw new ProjectNameAlreadyExistsException();
        }

        Project savedProject = projectRepository.save(project);

        return CreatedProject.from(savedProject);
    }
}