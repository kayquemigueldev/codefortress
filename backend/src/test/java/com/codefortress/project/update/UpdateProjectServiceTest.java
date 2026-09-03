package com.codefortress.project.update;

import com.codefortress.identity.user.User;
import com.codefortress.identity.user.UserRepository;
import com.codefortress.project.Project;
import com.codefortress.project.ProjectRepository;
import com.codefortress.project.ProjectStatus;
import com.codefortress.project.creation.ProjectNameAlreadyExistsException;
import com.codefortress.project.details.ProjectNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class UpdateProjectServiceTest {

    @Autowired
    private UpdateProjectService updateProjectService;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldUpdateProjectBelongingToOwner() {
        User owner = createUser(
                "owner@example.com",
                "Project Owner"
        );

        Project project = saveProject(
                owner,
                "Old Name",
                "Old description"
        );

        UpdatedProject updatedProject =
                updateProjectService.update(
                        new UpdateProjectCommand(
                                owner.getId(),
                                project.getId(),
                                "  New Name  ",
                                "  New description  "
                        )
                );

        assertThat(updatedProject.id())
                .isEqualTo(project.getId());
        assertThat(updatedProject.name())
                .isEqualTo("New Name");
        assertThat(updatedProject.description())
                .isEqualTo("New description");
        assertThat(updatedProject.status())
                .isEqualTo(ProjectStatus.ACTIVE);

        Project persistedProject = projectRepository
                .findById(project.getId())
                .orElseThrow();

        assertThat(persistedProject.getName())
                .isEqualTo("New Name");
        assertThat(persistedProject.getDescription())
                .isEqualTo("New description");
    }

    @Test
    void shouldRejectNameUsedByAnotherActiveProject() {
        User owner = createUser(
                "owner@example.com",
                "Project Owner"
        );

        Project firstProject = saveProject(
                owner,
                "First Project",
                null
        );

        saveProject(
                owner,
                "Existing Name",
                null
        );

        assertThatThrownBy(() ->
                updateProjectService.update(
                        new UpdateProjectCommand(
                                owner.getId(),
                                firstProject.getId(),
                                "  existing name  ",
                                null
                        )
                )
        ).isInstanceOf(
                ProjectNameAlreadyExistsException.class
        );

        Project unchangedProject = projectRepository
                .findById(firstProject.getId())
                .orElseThrow();

        assertThat(unchangedProject.getName())
                .isEqualTo("First Project");
    }

    @Test
    void shouldHideProjectBelongingToAnotherOwner() {
        User owner = createUser(
                "owner@example.com",
                "Project Owner"
        );

        User anotherUser = createUser(
                "another@example.com",
                "Another User"
        );

        Project project = saveProject(
                owner,
                "Private Project",
                null
        );

        assertThatThrownBy(() ->
                updateProjectService.update(
                        new UpdateProjectCommand(
                                anotherUser.getId(),
                                project.getId(),
                                "Stolen Project",
                                null
                        )
                )
        ).isInstanceOf(ProjectNotFoundException.class);

        assertThat(project.getName())
                .isEqualTo("Private Project");
    }

    @Test
    void shouldRejectArchivedProjectUpdate() {
        User owner = createUser(
                "owner@example.com",
                "Project Owner"
        );

        Project project = saveProject(
                owner,
                "Archived Project",
                null
        );

        project.archive();
        projectRepository.saveAndFlush(project);

        assertThatThrownBy(() ->
                updateProjectService.update(
                        new UpdateProjectCommand(
                                owner.getId(),
                                project.getId(),
                                "Updated Name",
                                null
                        )
                )
        ).isInstanceOf(ProjectNotFoundException.class);
    }

    private Project saveProject(
            User owner,
            String name,
            String description
    ) {
        Project project = Project.create(
                owner,
                name,
                description
        );

        return projectRepository.saveAndFlush(project);
    }

    private User createUser(
            String email,
            String displayName
    ) {
        User user = User.create(
                email,
                "test-password-hash",
                displayName
        );

        return userRepository.saveAndFlush(user);
    }
}