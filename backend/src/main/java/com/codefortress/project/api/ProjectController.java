package com.codefortress.project.api;

import com.codefortress.project.creation.CreateProjectCommand;
import com.codefortress.project.creation.CreateProjectService;
import com.codefortress.project.creation.CreatedProject;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects")
public class ProjectController {

    private final CreateProjectService createProjectService;

    public ProjectController(
            CreateProjectService createProjectService
    ) {
        this.createProjectService = createProjectService;
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
}