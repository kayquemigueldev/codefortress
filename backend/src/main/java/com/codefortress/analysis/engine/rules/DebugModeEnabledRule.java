package com.codefortress.analysis.engine.rules;

import com.codefortress.analysis.FindingCategory;
import com.codefortress.analysis.Severity;
import com.codefortress.analysis.discovery.SourceFileCategory;
import com.codefortress.analysis.engine.AnalysisContext;
import com.codefortress.analysis.engine.RuleMatch;
import com.codefortress.analysis.engine.RuleMetadata;
import com.codefortress.analysis.engine.ScannableFile;
import com.codefortress.analysis.engine.SecurityRule;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

public class DebugModeEnabledRule
        implements SecurityRule {

    private static final RuleMetadata METADATA =
            new RuleMetadata(
                    "CF-SEC-002",
                    "1.0.0",
                    "Debug Mode Enabled",
                    FindingCategory.CONFIGURATION,
                    Severity.MEDIUM,
                    "Detects application debug mode explicitly enabled in configuration files.",
                    "Debug mode may expose stack traces, internal application details, configuration data, or other sensitive information.",
                    "Disable debug mode in production and enable it only in controlled development environments."
            );

    private static final Set<String> DEBUG_KEYS =
            Set.of(
                    "debug",
                    "appdebug",
                    "springdebug",
                    "flaskdebug"
            );

    private static final Set<String> ENABLED_VALUES =
            Set.of(
                    "true",
                    "1",
                    "yes",
                    "on"
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

        List<String> lines =
                file.content()
                        .lines()
                        .toList();

        for (int index = 0;
             index < lines.size();
             index++) {

            String line = lines.get(index);

            if (isDebugEnabled(line)) {
                int lineNumber = index + 1;

                return List.of(
                        new RuleMatch(
                                METADATA.key(),
                                METADATA.defaultSeverity(),
                                file.normalizedPath(),
                                lineNumber,
                                lineNumber,
                                line.trim()
                        )
                );
            }
        }

        return List.of();
    }

    private boolean isDebugEnabled(
            String line
    ) {
        String trimmed = line.trim();

        if (trimmed.isBlank()
                || trimmed.startsWith("#")
                || trimmed.startsWith("//")
                || trimmed.startsWith(";")) {
            return false;
        }

        int separatorIndex =
                findSeparator(trimmed);

        if (separatorIndex < 0) {
            return false;
        }

        String key =
                normalizeKey(
                        trimmed.substring(
                                0,
                                separatorIndex
                        )
                );

        if (!DEBUG_KEYS.contains(key)) {
            return false;
        }

        String value =
                normalizeValue(
                        trimmed.substring(
                                separatorIndex + 1
                        )
                );

        return ENABLED_VALUES.contains(value);
    }

    private int findSeparator(
            String line
    ) {
        int equalsIndex =
                line.indexOf('=');

        int colonIndex =
                line.indexOf(':');

        if (equalsIndex < 0) {
            return colonIndex;
        }

        if (colonIndex < 0) {
            return equalsIndex;
        }

        return Math.min(
                equalsIndex,
                colonIndex
        );
    }

    private String normalizeKey(
            String key
    ) {
        return stripQuotes(key.trim())
                .toLowerCase(Locale.ROOT)
                .replace(".", "")
                .replace("_", "")
                .replace("-", "");
    }

    private String normalizeValue(
            String value
    ) {
        String normalized =
                value.trim();

        if (normalized.endsWith(",")) {
            normalized =
                    normalized.substring(
                            0,
                            normalized.length() - 1
                    ).trim();
        }

        return stripQuotes(normalized)
                .toLowerCase(Locale.ROOT);
    }

    private String stripQuotes(
            String value
    ) {
        if (value.length() < 2) {
            return value;
        }

        char first =
                value.charAt(0);

        char last =
                value.charAt(
                        value.length() - 1
                );

        boolean doubleQuoted =
                first == '"'
                        && last == '"';

        boolean singleQuoted =
                first == '\''
                        && last == '\'';

        if (doubleQuoted
                || singleQuoted) {
            return value.substring(
                    1,
                    value.length() - 1
            );
        }

        return value;
    }
}