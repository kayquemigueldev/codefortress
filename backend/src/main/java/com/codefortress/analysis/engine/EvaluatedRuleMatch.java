package com.codefortress.analysis.engine;

import java.util.Objects;

public record EvaluatedRuleMatch(
        RuleMetadata metadata,
        RuleMatch match
) {

    public EvaluatedRuleMatch {
        Objects.requireNonNull(
                metadata,
                "metadata must not be null"
        );

        Objects.requireNonNull(
                match,
                "match must not be null"
        );

        if (!metadata.key().equals(
                match.ruleKey()
        )) {
            throw new IllegalArgumentException(
                    "metadata key must match rule match key"
            );
        }
    }
}