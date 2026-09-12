package com.codefortress.analysis.engine.rules;

import com.codefortress.analysis.FindingCategory;
import com.codefortress.analysis.Severity;
import com.codefortress.analysis.discovery.SourceFileCategory;
import com.codefortress.analysis.engine.AnalysisContext;
import com.codefortress.analysis.engine.RuleMatch;
import com.codefortress.analysis.engine.RuleMetadata;
import com.codefortress.analysis.engine.ScannableFile;
import com.codefortress.analysis.engine.SecurityRule;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class ExposedManagementEndpointsRule
        implements SecurityRule {

    private static final RuleMetadata METADATA =
            new RuleMetadata(
                    "CF-SEC-008",
                    "1.0.0",
                    "Exposed Management Endpoints",
                    FindingCategory.CONFIGURATION,
                    Severity.HIGH,
                    "Detects management endpoint configuration that exposes all available web endpoints.",
                    "Exposing every management endpoint may reveal sensitive operational information or administrative functionality to unintended users.",
                    "Expose only the management endpoints that are required, such as health and info, and protect sensitive endpoints with appropriate authentication and network restrictions."
            );

    private static final String MANAGEMENT_EXPOSURE_KEY =
            "managementendpointswebexposureinclude";

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

        List<RuleMatch> directMatches =
                findDirectConfigurationMatches(
                        file,
                        lines
                );

        if (!directMatches.isEmpty()) {
            return directMatches;
        }

        return findYamlMatches(
                file,
                lines
        );
    }

    private List<RuleMatch> findDirectConfigurationMatches(
            ScannableFile file,
            List<String> lines
    ) {
        for (int index = 0;
             index < lines.size();
             index++) {

            String line =
                    lines.get(index);

            String trimmed =
                    line.trim();

            if (isIgnoredLine(trimmed)) {
                continue;
            }

            int separatorIndex =
                    findSeparator(trimmed);

            if (separatorIndex < 0) {
                continue;
            }

            String key =
                    normalizeKey(
                            trimmed.substring(
                                    0,
                                    separatorIndex
                            )
                    );

            if (!MANAGEMENT_EXPOSURE_KEY.equals(
                    key
            )) {
                continue;
            }

            String value =
                    trimmed.substring(
                            separatorIndex + 1
                    ).trim();

            if (isWildcardExposure(value)) {
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

    private List<RuleMatch> findYamlMatches(
            ScannableFile file,
            List<String> lines
    ) {
        List<YamlLevel> levels =
                new ArrayList<>();

        for (int index = 0;
             index < lines.size();
             index++) {

            String line =
                    lines.get(index);

            String trimmed =
                    line.trim();

            if (isIgnoredLine(trimmed)
                    || !trimmed.contains(":")) {
                continue;
            }

            int separatorIndex =
                    trimmed.indexOf(':');

            String rawKey =
                    trimmed.substring(
                            0,
                            separatorIndex
                    ).trim();

            String value =
                    trimmed.substring(
                            separatorIndex + 1
                    ).trim();

            String key =
                    normalizeKey(rawKey);

            int indent =
                    leadingWhitespaceCount(line);

            while (!levels.isEmpty()
                    && levels.getLast().indent()
                    >= indent) {
                levels.removeLast();
            }

            if (value.isBlank()) {
                levels.add(
                        new YamlLevel(
                                indent,
                                key
                        )
                );

                continue;
            }

            if (!"include".equals(key)
                    || !isWildcardExposure(value)) {
                continue;
            }

            String fullPath =
                    buildYamlPath(
                            levels,
                            key
                    );

            if (!MANAGEMENT_EXPOSURE_KEY.equals(
                    fullPath
            )) {
                continue;
            }

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

        return List.of();
    }

    private String buildYamlPath(
            List<YamlLevel> levels,
            String currentKey
    ) {
        StringBuilder path =
                new StringBuilder();

        for (YamlLevel level : levels) {
            path.append(
                    level.key()
            );
        }

        path.append(
                currentKey
        );

        return path.toString();
    }

    private boolean isWildcardExposure(
            String value
    ) {
        String normalized =
                stripQuotes(
                        value.trim()
                );

        if (isPlaceholder(normalized)) {
            return false;
        }

        if (normalized.startsWith("[")
                && normalized.endsWith("]")) {
            normalized =
                    normalized.substring(
                            1,
                            normalized.length() - 1
                    );
        }

        String[] values =
                normalized.split(",");

        for (String item : values) {
            String normalizedItem =
                    stripQuotes(
                            item.trim()
                    );

            if ("*".equals(
                    normalizedItem
            )) {
                return true;
            }
        }

        return false;
    }

    private boolean isIgnoredLine(
            String line
    ) {
        return line.isBlank()
                || line.startsWith("#")
                || line.startsWith("//")
                || line.startsWith(";");
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

    private int leadingWhitespaceCount(
            String line
    ) {
        int count = 0;

        while (count < line.length()
                && Character.isWhitespace(
                line.charAt(count)
        )) {
            count++;
        }

        return count;
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

    private boolean isPlaceholder(
            String value
    ) {
        return value.startsWith("${")
                && value.endsWith("}");
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

    private record YamlLevel(
            int indent,
            String key
    ) {
    }
}