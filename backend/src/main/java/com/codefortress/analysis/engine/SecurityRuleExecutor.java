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
                                        SecurityRule::key
                                )
                                .thenComparing(
                                        SecurityRule::version
                                )
                )
                .toList();
    }

    public List<RuleMatch> execute(
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

        List<RuleMatch> matches =
                new ArrayList<>();

        for (ScannableFile file
                : orderedFiles) {

            for (SecurityRule rule
                    : rules) {

                if (!rule.supports(file)) {
                    continue;
                }

                List<RuleMatch> ruleMatches =
                        Objects.requireNonNull(
                                rule.evaluate(
                                        file,
                                        context
                                ),
                                "rule matches must not be null"
                        );

                matches.addAll(
                        ruleMatches
                );
            }
        }

        return List.copyOf(matches);
    }
}