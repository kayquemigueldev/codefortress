package com.codefortress.project.listing;

import com.codefortress.identity.user.CurrentUserService;
import com.codefortress.project.ProjectRepository;
import com.codefortress.project.ProjectStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ListProjectsService {

    private final ProjectRepository projectRepository;
    private final CurrentUserService currentUserService;

    public ListProjectsService(
            ProjectRepository projectRepository,
            CurrentUserService currentUserService
    ) {
        this.projectRepository = projectRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional(readOnly = true)
    public List<ListedProject> list(UUID ownerId) {
        currentUserService.get(ownerId);

        return projectRepository
                .findAllByOwner_IdAndStatusOrderByCreatedAtDesc(
                        ownerId,
                        ProjectStatus.ACTIVE
                )
                .stream()
                .map(ListedProject::from)
                .toList();
    }
}