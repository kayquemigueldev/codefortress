package com.codefortress.project.archiving;

import com.codefortress.identity.user.User;
import com.codefortress.identity.user.UserRepository;
import com.codefortress.project.Project;
import com.codefortress.project.ProjectRepository;
import com.codefortress.project.ProjectStatus;
import com.codefortress.project.details.ProjectNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class ArchiveProjectServiceTest {

    @Autowired
    private ArchiveProjectService archiveProjectService;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldArchiveOwnedProject() {
        User owner = createUser(
                "owner@example.com",
                "Project Owner"
        );

        Project project = createProject(
                owner,
                "CodeFortress API"
        );

        archiveProjectService.archive(
                owner.getId(),
                project.getId()
        );

        Project archivedProject = projectRepository
                .findById(project.getId())
                .orElseThrow();

        assertThat(archivedProject.getStatus())
                .isEqualTo(ProjectStatus.ARCHIVED);
    }

    @Test
    void shouldHideProjectFromAnotherOwner() {
        User owner = createUser(
                "owner@example.com",
                "Project Owner"
        );

        User anotherUser = createUser(
                "another@example.com",
                "Another User"
        );

        Project project = createProject(
                owner,
                "Private Project"
        );

        assertThatThrownBy(() ->
                archiveProjectService.archive(
                        anotherUser.getId(),
                        project.getId()
                )
        ).isInstanceOf(ProjectNotFoundException.class);

        Project persistedProject = projectRepository
                .findById(project.getId())
                .orElseThrow();

        assertThat(persistedProject.getStatus())
                .isEqualTo(ProjectStatus.ACTIVE);
    }

    @Test
    void shouldAllowArchivingAnAlreadyArchivedProject() {
        User owner = createUser(
                "owner@example.com",
                "Project Owner"
        );

        Project project = createProject(
                owner,
                "Archived Project"
        );

        project.archive();
        projectRepository.saveAndFlush(project);

        assertThatCode(() ->
                archiveProjectService.archive(
                        owner.getId(),
                        project.getId()
                )
        ).doesNotThrowAnyException();

        Project archivedProject = projectRepository
                .findById(project.getId())
                .orElseThrow();

        assertThat(archivedProject.getStatus())
                .isEqualTo(ProjectStatus.ARCHIVED);
    }

    private User createUser(String email, String displayName) {
        User user = User.create(
                email,
                "test-password-hash",
                displayName
        );

        return userRepository.saveAndFlush(user);
    }

    private Project createProject(User owner, String name) {
        Project project = Project.create(
                owner,
                name,
                "Project used during automated tests"
        );

        return projectRepository.saveAndFlush(project);
    }
}