package com.codefortress.project.listing;

import com.codefortress.project.Project;
import com.codefortress.project.ProjectStatus;

import java.time.Instant;
import java.util.UUID;

public record ListedProject(
        UUID id,
        String name,
        String description,
        ProjectStatus status,
        Instant createdAt,
        Instant updatedAt
) {

    public static ListedProject from(Project project) {
        return new ListedProject(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getStatus(),
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }
}