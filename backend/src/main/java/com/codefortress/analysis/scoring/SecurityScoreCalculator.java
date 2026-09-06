package com.codefortress.analysis.scoring;

import com.codefortress.analysis.Severity;
import com.codefortress.analysis.engine.EvaluatedRuleMatch;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class SecurityScoreCalculator {

    private static final int INITIAL_SCORE = 100;

    private static final Map<Severity, Integer> WEIGHTS =
            Map.of(
                    Severity.CRITICAL, 18,
                    Severity.HIGH, 8,
                    Severity.MEDIUM, 3,
                    Severity.LOW, 1
            );

    public int calculate(
            List<EvaluatedRuleMatch> matches
    ) {
        Objects.requireNonNull(
                matches,
                "matches must not be null"
        );

        EnumMap<Severity, Integer> counts =
                new EnumMap<>(
                        Severity.class
                );

        for (EvaluatedRuleMatch evaluatedMatch : matches) {
            Objects.requireNonNull(
                    evaluatedMatch,
                    "evaluatedMatch must not be null"
            );

            Severity severity =
                    evaluatedMatch
                            .match()
                            .severity();

            counts.merge(
                    severity,
                    1,
                    Integer::sum
            );
        }

        double totalPenalty = 0.0;

        for (Map.Entry<Severity, Integer> entry
                : counts.entrySet()) {

            int weight =
                    WEIGHTS.get(
                            entry.getKey()
                    );

            int count =
                    entry.getValue();

            totalPenalty +=
                    calculatePenalty(
                            weight,
                            count
                    );
        }

        int score =
                INITIAL_SCORE
                        - (int) Math.round(
                        totalPenalty
                );

        score =
                Math.max(
                        0,
                        Math.min(
                                INITIAL_SCORE,
                                score
                        )
                );

        return applySeverityCap(
                score,
                counts
        );
    }

    private double calculatePenalty(
            int weight,
            int count
    ) {
        return weight
                * (
                1.0
                        + 0.5
                        * Math.log(count)
        );
    }

    private int applySeverityCap(
            int score,
            Map<Severity, Integer> counts
    ) {
        if (counts.containsKey(
                Severity.CRITICAL
        )) {
            return Math.min(
                    score,
                    49
            );
        }

        if (counts.containsKey(
                Severity.HIGH
        )) {
            return Math.min(
                    score,
                    74
            );
        }

        if (counts.containsKey(
                Severity.MEDIUM
        )) {
            return Math.min(
                    score,
                    89
            );
        }

        return score;
    }
}