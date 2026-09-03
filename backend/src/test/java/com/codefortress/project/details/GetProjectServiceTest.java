package com.codefortress.project.details;

import com.codefortress.identity.user.User;
import com.codefortress.identity.user.UserRepository;
import com.codefortress.project.Project;
import com.codefortress.project.ProjectRepository;
import com.codefortress.project.ProjectStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class GetProjectServiceTest {

    @Autowired
    private GetProjectService getProjectService;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldGetProjectBelongingToOwner() {
        User owner = createUser(
                "owner@example.com",
                "Project Owner"
        );

        Project project = saveProject(
                owner,
                "CodeFortress",
                "Security analysis platform"
        );

        ProjectDetails details = getProjectService.get(
                owner.getId(),
                project.getId()
        );

        assertThat(details.id())
                .isEqualTo(project.getId());
        assertThat(details.name())
                .isEqualTo("CodeFortress");
        assertThat(details.description())
                .isEqualTo("Security analysis platform");
        assertThat(details.status())
                .isEqualTo(ProjectStatus.ACTIVE);
        assertThat(details.createdAt()).isNotNull();
        assertThat(details.updatedAt()).isNotNull();
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
                getProjectService.get(
                        anotherUser.getId(),
                        project.getId()
                )
        ).isInstanceOf(ProjectNotFoundException.class)
                .hasMessage("Project not found");
    }

    @Test
    void shouldRejectUnknownProject() {
        User owner = createUser(
                "owner@example.com",
                "Project Owner"
        );

        UUID unknownProjectId = UUID.randomUUID();

        assertThatThrownBy(() ->
                getProjectService.get(
                        owner.getId(),
                        unknownProjectId
                )
        ).isInstanceOf(ProjectNotFoundException.class)
                .hasMessage("Project not found");
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