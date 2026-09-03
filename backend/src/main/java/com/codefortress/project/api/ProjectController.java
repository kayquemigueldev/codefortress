package com.codefortress.project.api;

import com.codefortress.project.creation.CreateProjectCommand;
import com.codefortress.project.creation.CreateProjectService;
import com.codefortress.project.creation.CreatedProject;
import com.codefortress.project.details.GetProjectService;
import com.codefortress.project.details.ProjectDetails;
import com.codefortress.project.listing.ListProjectsService;
import com.codefortress.project.update.UpdateProjectCommand;
import com.codefortress.project.update.UpdateProjectService;
import com.codefortress.project.update.UpdatedProject;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects")
public class ProjectController {

    private final CreateProjectService createProjectService;
    private final ListProjectsService listProjectsService;
    private final GetProjectService getProjectService;
    private final UpdateProjectService updateProjectService;

    public ProjectController(
            CreateProjectService createProjectService,
            ListProjectsService listProjectsService,
            GetProjectService getProjectService,
            UpdateProjectService updateProjectService
    ) {
        this.createProjectService = createProjectService;
        this.listProjectsService = listProjectsService;
        this.getProjectService = getProjectService;
        this.updateProjectService = updateProjectService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectResponse createProject(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateProjectRequest request
    ) {
        UUID ownerId = UUID.fromString(jwt.getSubject());

        CreatedProject createdProject =
                createProjectService.create(
                        new CreateProjectCommand(
                                ownerId,
                                request.name(),
                                request.description()
                        )
                );

        return ProjectResponse.from(createdProject);
    }

    @GetMapping
    public List<ProjectResponse> listProjects(
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID ownerId = UUID.fromString(jwt.getSubject());

        return listProjectsService
                .list(ownerId)
                .stream()
                .map(ProjectResponse::from)
                .toList();
    }

    @GetMapping("/{projectId}")
    public ProjectResponse getProject(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID projectId
    ) {
        UUID ownerId = UUID.fromString(jwt.getSubject());

        ProjectDetails project = getProjectService.get(
                ownerId,
                projectId
        );

        return ProjectResponse.from(project);
    }

    @PutMapping("/{projectId}")
    public ProjectResponse updateProject(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID projectId,
            @Valid @RequestBody UpdateProjectRequest request
    ) {
        UUID ownerId = UUID.fromString(jwt.getSubject());

        UpdatedProject updatedProject =
                updateProjectService.update(
                        new UpdateProjectCommand(
                                ownerId,
                                projectId,
                                request.name(),
                                request.description()
                        )
                );

        return ProjectResponse.from(updatedProject);
    }
}