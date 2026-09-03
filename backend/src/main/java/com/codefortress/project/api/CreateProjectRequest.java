package com.codefortress.project.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateProjectRequest(

        @NotBlank(message = "name must not be blank")
        @Size(
                max = 120,
                message = "name must have at most 120 characters"
        )
        String name,

        @Size(
                max = 500,
                message = "description must have at most 500 characters"
        )
        String description
) {
}