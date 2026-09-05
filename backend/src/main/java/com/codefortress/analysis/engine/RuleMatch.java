package com.codefortress.analysis.engine;

import java.util.Objects;

public record RuleMatch(
        String ruleKey,
        String filePath,
        int startLine,
        int endLine,
        String redactedEvidence
) {

    public RuleMatch {
        if (ruleKey == null
                || ruleKey.isBlank()) {
            throw new IllegalArgumentException(
                    "ruleKey must not be blank"
            );
        }

        if (filePath == null
                || filePath.isBlank()) {
            throw new IllegalArgumentException(
                    "filePath must not be blank"
            );
        }

        if (startLine < 1) {
            throw new IllegalArgumentException(
                    "startLine must be greater than zero"
            );
        }

        if (endLine < startLine) {
            throw new IllegalArgumentException(
                    "endLine must not be before startLine"
            );
        }

        Objects.requireNonNull(
                redactedEvidence,
                "redactedEvidence must not be null"
        );
    }
}