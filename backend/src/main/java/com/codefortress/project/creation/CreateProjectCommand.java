package com.codefortress.project.creation;

import java.util.UUID;

public record CreateProjectCommand(
        UUID ownerId,
        String name,
        String description
) {

    public CreateProjectCommand {
        if (ownerId == null) {
            throw new IllegalArgumentException(
                    "ownerId must not be null"
            );
        }
    }
}