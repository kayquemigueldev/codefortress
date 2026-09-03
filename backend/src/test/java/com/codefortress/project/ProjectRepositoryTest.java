package com.codefortress.project;

import com.codefortress.identity.user.User;
import com.codefortress.identity.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class ProjectRepositoryTest {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldPersistProjectForOwner() {
        User owner = createOwner();

        Project project = Project.create(
                owner,
                "  CodeFortress API  ",
                "  Security analysis backend  "
        );

        projectRepository.saveAndFlush(project);

        Project persistedProject = projectRepository
                .findByIdAndOwner_Id(
                        project.getId(),
                        owner.getId()
                )
                .orElseThrow();

        assertThat(persistedProject.getId()).isNotNull();
        assertThat(persistedProject.getOwnerId())
                .isEqualTo(owner.getId());
        assertThat(persistedProject.getName())
                .isEqualTo("CodeFortress API");
        assertThat(persistedProject.getDescription())
                .isEqualTo("Security analysis backend");
        assertThat(persistedProject.getStatus())
                .isEqualTo(ProjectStatus.ACTIVE);
        assertThat(persistedProject.getCreatedAt()).isNotNull();
        assertThat(persistedProject.getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldArchiveProject() {
        User owner = createOwner();

        Project project = Project.create(
                owner,
                "CodeFortress API",
                null
        );

        projectRepository.saveAndFlush(project);

        assertThat(
                projectRepository
                        .existsByOwner_IdAndNameIgnoreCaseAndStatus(
                                owner.getId(),
                                "codefortress api",
                                ProjectStatus.ACTIVE
                        )
        ).isTrue();

        project.archive();
        projectRepository.saveAndFlush(project);

        assertThat(
                projectRepository
                        .existsByOwner_IdAndNameIgnoreCaseAndStatus(
                                owner.getId(),
                                "codefortress api",
                                ProjectStatus.ACTIVE
                        )
        ).isFalse();

        assertThat(
                projectRepository
                        .existsByOwner_IdAndNameIgnoreCaseAndStatus(
                                owner.getId(),
                                "codefortress api",
                                ProjectStatus.ARCHIVED
                        )
        ).isTrue();
    }

    private User createOwner() {
        User owner = User.create(
                "owner@example.com",
                "test-password-hash",
                "Project Owner"
        );

        return userRepository.saveAndFlush(owner);
    }
}