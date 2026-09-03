package com.codefortress.project.listing;

import com.codefortress.identity.user.CurrentUserUnavailableException;
import com.codefortress.identity.user.User;
import com.codefortress.identity.user.UserRepository;
import com.codefortress.project.Project;
import com.codefortress.project.ProjectRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class ListProjectsServiceTest {

    @Autowired
    private ListProjectsService listProjectsService;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldListOnlyActiveProjectsFromOwnerNewestFirst() {
        User owner = createUser(
                "owner@example.com",
                "Project Owner"
        );

        User otherOwner = createUser(
                "other@example.com",
                "Other Owner"
        );

        Project firstProject = saveProject(
                owner,
                "First Project"
        );

        Project archivedProject = saveProject(
                owner,
                "Archived Project"
        );

        archivedProject.archive();
        projectRepository.saveAndFlush(archivedProject);

        Project secondProject = saveProject(
                owner,
                "Second Project"
        );

        saveProject(
                otherOwner,
                "Other Owner Project"
        );

        List<ListedProject> projects =
                listProjectsService.list(owner.getId());

        assertThat(projects)
                .extracting(ListedProject::id)
                .containsExactly(
                        secondProject.getId(),
                        firstProject.getId()
                );

        assertThat(projects)
                .extracting(ListedProject::name)
                .containsExactly(
                        "Second Project",
                        "First Project"
                );
    }

    @Test
    void shouldRejectUnavailableOwner() {
        UUID unavailableOwnerId = UUID.randomUUID();

        assertThatThrownBy(() ->
                listProjectsService.list(unavailableOwnerId)
        ).isInstanceOf(
                CurrentUserUnavailableException.class
        );
    }

    private Project saveProject(
            User owner,
            String name
    ) {
        Project project = Project.create(
                owner,
                name,
                null
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