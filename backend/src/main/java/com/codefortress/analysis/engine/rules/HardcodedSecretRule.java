package com.codefortress.analysis.engine.rules;

import com.codefortress.analysis.Severity;
import com.codefortress.analysis.discovery.SourceFileCategory;
import com.codefortress.analysis.engine.AnalysisContext;
import com.codefortress.analysis.engine.RuleMatch;
import com.codefortress.analysis.engine.ScannableFile;
import com.codefortress.analysis.engine.SecurityRule;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HardcodedSecretRule
        implements SecurityRule {

    private static final String RULE_KEY =
            "CF-SEC-001";

    private static final String RULE_VERSION =
            "1.0.0";

    private static final String REDACTED_VALUE =
            "********";

    private static final Pattern SOURCE_ASSIGNMENT =
            Pattern.compile(
                    "(?i)\\b"
                            + "(password|passwd|pwd|"
                            + "api[_-]?key|apikey|"
                            + "secret|token|"
                            + "jwt[_-]?secret|jwtsecret)"
                            + "\\b\\s*=\\s*"
                            + "([\"'])"
                            + "([^\"'\\r\\n]+)"
                            + "\\2"
            );

    private static final Pattern CONFIG_ASSIGNMENT =
            Pattern.compile(
                    "(?i)^\\s*"
                            + "([a-z0-9_.-]*"
                            + "(?:password|passwd|pwd|"
                            + "api[_-]?key|apikey|"
                            + "secret|token)"
                            + "[a-z0-9_.-]*)"
                            + "\\s*=\\s*(.+?)\\s*$"
            );

    @Override
    public String key() {
        return RULE_KEY;
    }

    @Override
    public String version() {
        return RULE_VERSION;
    }

    @Override
    public boolean supports(
            ScannableFile file
    ) {
        Objects.requireNonNull(
                file,
                "file must not be null"
        );

        return file.category()
                == SourceFileCategory.SOURCE_CODE
                || file.category()
                == SourceFileCategory.CONFIGURATION;
    }

    @Override
    public List<RuleMatch> evaluate(
            ScannableFile file,
            AnalysisContext context
    ) {
        Objects.requireNonNull(
                file,
                "file must not be null"
        );

        Objects.requireNonNull(
                context,
                "context must not be null"
        );

        if (!supports(file)) {
            return List.of();
        }

        List<RuleMatch> matches =
                new ArrayList<>();

        List<String> lines =
                file.content()
                        .lines()
                        .toList();

        for (int index = 0;
             index < lines.size();
             index++) {
            String line = lines.get(index);
            int lineNumber = index + 1;

            if (file.category()
                    == SourceFileCategory.SOURCE_CODE) {
                detectSourceSecret(
                        file,
                        line,
                        lineNumber,
                        matches
                );
            }

            if (file.category()
                    == SourceFileCategory.CONFIGURATION) {
                detectConfigurationSecret(
                        file,
                        line,
                        lineNumber,
                        matches
                );
            }
        }

        return List.copyOf(matches);
    }

    private void detectSourceSecret(
            ScannableFile file,
            String line,
            int lineNumber,
            List<RuleMatch> matches
    ) {
        Matcher matcher =
                SOURCE_ASSIGNMENT.matcher(line);

        while (matcher.find()) {
            String secret = matcher.group(3);

            if (shouldIgnore(secret)) {
                continue;
            }

            String redactedEvidence =
                    redact(
                            line,
                            matcher.start(3),
                            matcher.end(3)
                    );

            matches.add(
                    match(
                            file,
                            lineNumber,
                            redactedEvidence
                    )
            );
        }
    }

    private void detectConfigurationSecret(
            ScannableFile file,
            String line,
            int lineNumber,
            List<RuleMatch> matches
    ) {
        Matcher matcher =
                CONFIG_ASSIGNMENT.matcher(line);

        if (!matcher.matches()) {
            return;
        }

        String secret = matcher.group(2);

        if (shouldIgnore(secret)) {
            return;
        }

        String redactedEvidence =
                redact(
                        line,
                        matcher.start(2),
                        matcher.end(2)
                );

        matches.add(
                match(
                        file,
                        lineNumber,
                        redactedEvidence
                )
        );
    }

    private boolean shouldIgnore(
            String value
    ) {
        if (value == null
                || value.isBlank()) {
            return true;
        }

        String trimmed = value.trim();

        return trimmed.startsWith("${")
                && trimmed.endsWith("}");
    }

    private String redact(
            String line,
            int start,
            int end
    ) {
        return line.substring(0, start)
                + REDACTED_VALUE
                + line.substring(end);
    }

    private RuleMatch match(
            ScannableFile file,
            int lineNumber,
            String redactedEvidence
    ) {
        return new RuleMatch(
                RULE_KEY,
                Severity.CRITICAL,
                file.normalizedPath(),
                lineNumber,
                lineNumber,
                redactedEvidence
        );
    }
}