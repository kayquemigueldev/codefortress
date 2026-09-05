package com.codefortress.analysis.engine.rules;

import com.codefortress.analysis.Severity;
import com.codefortress.analysis.discovery.SourceFileCategory;
import com.codefortress.analysis.engine.AnalysisContext;
import com.codefortress.analysis.engine.RuleMatch;
import com.codefortress.analysis.engine.ScannableFile;
import com.codefortress.analysis.engine.SecurityRule;
import com.codefortress.analysis.FindingCategory;
import com.codefortress.analysis.engine.RuleMetadata;

import java.util.Locale;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HardcodedSecretRule
        implements SecurityRule {

    private static final RuleMetadata METADATA =
            new RuleMetadata(
                    "CF-SEC-001",
                    "1.0.0",
                    "Hardcoded Secret",
                    FindingCategory.SECRETS,
                    Severity.CRITICAL,
                    "Detects passwords, tokens, API keys, and other secrets embedded directly in source code or configuration.",
                    "Exposed secrets may allow unauthorized access to systems, services, or sensitive data.",
                    "Move secrets to environment variables or a dedicated secret manager and rotate exposed credentials."
            );

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


    @Override
    public RuleMetadata metadata() {
        return METADATA;
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
        int separatorIndex =
                line.indexOf('=');

        if (separatorIndex < 0) {
            return;
        }

        String key =
                line.substring(
                        0,
                        separatorIndex
                ).trim();

        String secret =
                line.substring(
                        separatorIndex + 1
                ).trim();

        if (!isSensitiveConfigurationKey(key)
                || shouldIgnore(secret)) {
            return;
        }

        String redactedEvidence =
                line.substring(
                        0,
                        separatorIndex + 1
                )
                        + REDACTED_VALUE;

        matches.add(
                match(
                        file,
                        lineNumber,
                        redactedEvidence
                )
        );
    }

    private boolean isSensitiveConfigurationKey(
            String key
    ) {
        String normalizedKey =
                key.toLowerCase(Locale.ROOT)
                        .replace(".", "")
                        .replace("_", "")
                        .replace("-", "");

        return normalizedKey.contains("password")
                || normalizedKey.contains("passwd")
                || normalizedKey.contains("pwd")
                || normalizedKey.contains("apikey")
                || normalizedKey.contains("secret")
                || normalizedKey.contains("token");
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
                METADATA.key(),
                METADATA.defaultSeverity(),
                file.normalizedPath(),
                lineNumber,
                lineNumber,
                redactedEvidence
        );
    }
}