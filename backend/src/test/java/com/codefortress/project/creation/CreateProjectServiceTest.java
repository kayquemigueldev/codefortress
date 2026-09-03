package com.codefortress.project.creation;

import com.codefortress.identity.user.User;
import com.codefortress.identity.user.UserRepository;
import com.codefortress.project.Project;
import com.codefortress.project.ProjectRepository;
import com.codefortress.project.ProjectStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class CreateProjectServiceTest {

    @Autowired
    private CreateProjectService createProjectService;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldCreateProjectForOwner() {
        User owner = createUser(
                "owner@example.com",
                "Project Owner"
        );

        CreatedProject createdProject =
                createProjectService.create(
                        new CreateProjectCommand(
                                owner.getId(),
                                "  CodeFortress API  ",
                                "  Security analysis backend  "
                        )
                );

        Project persistedProject = projectRepository
                .findByIdAndOwner_Id(
                        createdProject.id(),
                        owner.getId()
                )
                .orElseThrow();

        assertThat(createdProject.id()).isNotNull();
        assertThat(createdProject.ownerId())
                .isEqualTo(owner.getId());
        assertThat(createdProject.name())
                .isEqualTo("CodeFortress API");
        assertThat(createdProject.description())
                .isEqualTo("Security analysis backend");
        assertThat(createdProject.status())
                .isEqualTo(ProjectStatus.ACTIVE);
        assertThat(createdProject.createdAt()).isNotNull();
        assertThat(createdProject.updatedAt()).isNotNull();

        assertThat(persistedProject.getOwnerId())
                .isEqualTo(owner.getId());
    }

    @Test
    void shouldRejectDuplicatedActiveNameIgnoringCaseAndSpaces() {
        User owner = createUser(
                "owner@example.com",
                "Project Owner"
        );

        createProjectService.create(
                new CreateProjectCommand(
                        owner.getId(),
                        "CodeFortress API",
                        null
                )
        );

        assertThatThrownBy(() ->
                createProjectService.create(
                        new CreateProjectCommand(
                                owner.getId(),
                                "  codefortress api  ",
                                "Another description"
                        )
                )
        ).isInstanceOf(
                ProjectNameAlreadyExistsException.class
        );

        assertThat(projectRepository.count()).isEqualTo(1);
    }

    @Test
    void shouldAllowSameProjectNameForDifferentOwners() {
        User firstOwner = createUser(
                "first@example.com",
                "First Owner"
        );

        User secondOwner = createUser(
                "second@example.com",
                "Second Owner"
        );

        createProjectService.create(
                new CreateProjectCommand(
                        firstOwner.getId(),
                        "CodeFortress",
                        null
                )
        );

        createProjectService.create(
                new CreateProjectCommand(
                        secondOwner.getId(),
                        "CodeFortress",
                        null
                )
        );

        assertThat(projectRepository.count()).isEqualTo(2);
    }

    @Test
    void shouldAllowNameReuseAfterProjectIsArchived() {
        User owner = createUser(
                "owner@example.com",
                "Project Owner"
        );

        CreatedProject firstProject =
                createProjectService.create(
                        new CreateProjectCommand(
                                owner.getId(),
                                "CodeFortress",
                                null
                        )
                );

        Project persistedProject = projectRepository
                .findById(firstProject.id())
                .orElseThrow();

        persistedProject.archive();
        projectRepository.saveAndFlush(persistedProject);

        CreatedProject secondProject =
                createProjectService.create(
                        new CreateProjectCommand(
                                owner.getId(),
                                "codefortress",
                                "New active project"
                        )
                );

        assertThat(secondProject.status())
                .isEqualTo(ProjectStatus.ACTIVE);
        assertThat(projectRepository.count()).isEqualTo(2);
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