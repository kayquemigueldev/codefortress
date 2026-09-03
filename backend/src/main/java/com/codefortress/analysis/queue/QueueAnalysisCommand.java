package com.codefortress.analysis.queue;

public record QueueAnalysisCommand(
        String sourceReference,
        String sourceFilename
) {

    public QueueAnalysisCommand {
        sourceReference = requireText(
                sourceReference,
                "sourceReference"
        );

        sourceFilename = requireText(
                sourceFilename,
                "sourceFilename"
        );

        if (!sourceReference.matches("[a-f0-9]{64}")) {
            throw new IllegalArgumentException(
                    "sourceReference must be a lowercase SHA-256 hash"
            );
        }

        if (sourceFilename.length() > 255) {
            throw new IllegalArgumentException(
                    "sourceFilename must not exceed 255 characters"
            );
        }
    }

    private static String requireText(
            String value,
            String fieldName
    ) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    fieldName + " must not be blank"
            );
        }

        return value.trim();
    }
}