package com.codefortress.project.update;

import java.util.UUID;

public record UpdateProjectCommand(
        UUID ownerId,
        UUID projectId,
        String name,
        String description
) {

    public UpdateProjectCommand {
        if (ownerId == null) {
            throw new IllegalArgumentException(
                    "ownerId must not be null"
            );
        }

        if (projectId == null) {
            throw new IllegalArgumentException(
                    "projectId must not be null"
            );
        }
    }
}