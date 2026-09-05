package com.codefortress.analysis.engine;

import com.codefortress.analysis.FindingCategory;
import com.codefortress.analysis.Severity;

import java.util.Objects;

public record RuleMetadata(
        String key,
        String version,
        String title,
        FindingCategory category,
        Severity defaultSeverity,
        String description,
        String impact,
        String recommendation
) {

    public RuleMetadata {
        requireText(
                key,
                "key must not be blank"
        );

        requireText(
                version,
                "version must not be blank"
        );

        requireText(
                title,
                "title must not be blank"
        );

        Objects.requireNonNull(
                category,
                "category must not be null"
        );

        Objects.requireNonNull(
                defaultSeverity,
                "defaultSeverity must not be null"
        );

        requireText(
                description,
                "description must not be blank"
        );

        requireText(
                impact,
                "impact must not be blank"
        );

        requireText(
                recommendation,
                "recommendation must not be blank"
        );
    }

    private static void requireText(
            String value,
            String message
    ) {
        if (value == null
                || value.isBlank()) {
            throw new IllegalArgumentException(
                    message
            );
        }
    }
}