package com.codefortress.project.api;

import com.codefortress.project.ProjectStatus;
import com.codefortress.project.creation.CreatedProject;

import java.time.Instant;
import java.util.UUID;

public record ProjectResponse(
        UUID id,
        String name,
        String description,
        ProjectStatus status,
        Instant createdAt,
        Instant updatedAt
) {

    public static ProjectResponse from(
            CreatedProject project
    ) {
        return new ProjectResponse(
                project.id(),
                project.name(),
                project.description(),
                project.status(),
                project.createdAt(),
                project.updatedAt()
        );
    }
}