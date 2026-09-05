package com.codefortress.analysis.engine;

import java.util.Objects;
import java.util.UUID;

public record AnalysisContext(
        UUID analysisId,
        String ruleSetVersion
) {

    public AnalysisContext {
        Objects.requireNonNull(
                analysisId,
                "analysisId must not be null"
        );

        if (ruleSetVersion == null
                || ruleSetVersion.isBlank()) {
            throw new IllegalArgumentException(
                    "ruleSetVersion must not be blank"
            );
        }
    }
}