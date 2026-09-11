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

public class PermissiveCorsConfigurationRule
        implements SecurityRule {

    private static final RuleMetadata METADATA =
            new RuleMetadata(
                    "CF-SEC-004",
                    "1.0.0",
                    "Permissive CORS Configuration",
                    FindingCategory.CONFIGURATION,
                    Severity.MEDIUM,
                    "Detects CORS configuration that allows requests from any origin.",
                    "Allowing every origin may expose application resources to untrusted websites and increase the risk of unauthorized cross-origin access.",
                    "Restrict allowed CORS origins to explicitly trusted domains instead of using a wildcard."
            );

    private static final Set<String>
            CORS_ORIGIN_KEYS =
            Set.of(
                    "corsallowedorigins",
                    "corsallowedorigin",
                    "corsorigins",
                    "corsorigin",
                    "allowedorigins",
                    "allowedorigin"
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

            if (isPermissiveCorsConfiguration(
                    line
            )) {
                int lineNumber =
                        index + 1;

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

    private boolean isPermissiveCorsConfiguration(
            String line
    ) {
        String trimmed =
                line.trim();

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

        if (!CORS_ORIGIN_KEYS.contains(key)) {
            return false;
        }

        String value =
                trimmed.substring(
                        separatorIndex + 1
                ).trim();

        if (isPlaceholder(value)) {
            return false;
        }

        return containsWildcardOrigin(
                value
        );
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
        return stripQuotes(
                key.trim()
        )
                .toLowerCase(Locale.ROOT)
                .replace(".", "")
                .replace("_", "")
                .replace("-", "");
    }

    private boolean containsWildcardOrigin(
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

        normalized =
                normalized
                        .replace("[", "")
                        .replace("]", "");

        String[] origins =
                normalized.split(",");

        for (String origin : origins) {
            String normalizedOrigin =
                    stripQuotes(
                            origin.trim()
                    );

            if ("*".equals(
                    normalizedOrigin
            )) {
                return true;
            }
        }

        return false;
    }

    private boolean isPlaceholder(
            String value
    ) {
        String normalized =
                stripQuotes(
                        value.trim()
                );

        return normalized.startsWith("${")
                && normalized.endsWith("}");
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