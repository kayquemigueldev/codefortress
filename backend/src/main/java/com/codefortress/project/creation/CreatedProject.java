package com.codefortress.project.creation;

import com.codefortress.project.Project;
import com.codefortress.project.ProjectStatus;

import java.time.Instant;
import java.util.UUID;

public record CreatedProject(
        UUID id,
        UUID ownerId,
        String name,
        String description,
        ProjectStatus status,
        Instant createdAt,
        Instant updatedAt
) {

    public static CreatedProject from(Project project) {
        return new CreatedProject(
                project.getId(),
                project.getOwnerId(),
                project.getName(),
                project.getDescription(),
                project.getStatus(),
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }
}