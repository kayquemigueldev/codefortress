package com.codefortress.analysis.engine;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class SecurityRuleExecutor {

    private final List<SecurityRule> rules;

    public SecurityRuleExecutor(
            List<SecurityRule> rules
    ) {
        Objects.requireNonNull(
                rules,
                "rules must not be null"
        );

        this.rules = rules.stream()
                .map(rule ->
                        Objects.requireNonNull(
                                rule,
                                "rule must not be null"
                        )
                )
                .sorted(
                        Comparator
                                .comparing(
                                        (SecurityRule rule) ->
                                                rule.metadata().key()
                                )
                                .thenComparing(
                                        rule ->
                                                rule.metadata().version()
                                )
                )
                .toList();
    }

    public List<EvaluatedRuleMatch> execute(
            List<ScannableFile> files,
            AnalysisContext context
    ) {
        Objects.requireNonNull(
                files,
                "files must not be null"
        );

        Objects.requireNonNull(
                context,
                "context must not be null"
        );

        List<ScannableFile> orderedFiles =
                files.stream()
                        .map(file ->
                                Objects.requireNonNull(
                                        file,
                                        "file must not be null"
                                )
                        )
                        .sorted(
                                Comparator.comparing(
                                        ScannableFile::normalizedPath
                                )
                        )
                        .toList();

        List<EvaluatedRuleMatch> matches =
                new ArrayList<>();

        for (ScannableFile file
                : orderedFiles) {

            for (SecurityRule rule
                    : rules) {

                if (!rule.supports(file)) {
                    continue;
                }

                RuleMetadata metadata =
                        Objects.requireNonNull(
                                rule.metadata(),
                                "rule metadata must not be null"
                        );

                List<RuleMatch> ruleMatches =
                        Objects.requireNonNull(
                                rule.evaluate(
                                        file,
                                        context
                                ),
                                "rule matches must not be null"
                        );

                for (RuleMatch ruleMatch
                        : ruleMatches) {

                    matches.add(
                            new EvaluatedRuleMatch(
                                    metadata,
                                    Objects.requireNonNull(
                                            ruleMatch,
                                            "rule match must not be null"
                                    )
                            )
                    );
                }
            }
        }

        return List.copyOf(matches);
    }
}