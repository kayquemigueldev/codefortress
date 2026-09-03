package com.codefortress.project.details;

import com.codefortress.project.Project;
import com.codefortress.project.ProjectStatus;

import java.time.Instant;
import java.util.UUID;

public record ProjectDetails(
        UUID id,
        String name,
        String description,
        ProjectStatus status,
        Instant createdAt,
        Instant updatedAt
) {

    public static ProjectDetails from(Project project) {
        return new ProjectDetails(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getStatus(),
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }
}