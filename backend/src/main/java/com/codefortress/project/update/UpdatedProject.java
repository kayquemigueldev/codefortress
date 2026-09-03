package com.codefortress.project.update;

import com.codefortress.project.Project;
import com.codefortress.project.ProjectStatus;

import java.time.Instant;
import java.util.UUID;

public record UpdatedProject(
        UUID id,
        String name,
        String description,
        ProjectStatus status,
        Instant createdAt,
        Instant updatedAt
) {

    public static UpdatedProject from(Project project) {
        return new UpdatedProject(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getStatus(),
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }
}