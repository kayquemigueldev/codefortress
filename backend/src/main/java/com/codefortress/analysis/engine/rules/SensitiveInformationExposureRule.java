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
import java.util.Objects;
import java.util.regex.Pattern;

public class SensitiveInformationExposureRule
        implements SecurityRule {

    private static final RuleMetadata METADATA =
            new RuleMetadata(
                    "CF-SEC-007",
                    "1.0.0",
                    "Sensitive Information Exposure",
                    FindingCategory.CODE,
                    Severity.HIGH,
                    "Detects potentially sensitive values being written to application logs or standard output.",
                    "Exposing passwords, tokens, secrets, API keys, or credentials in logs may allow unauthorized users to obtain sensitive information.",
                    "Avoid logging sensitive values. Remove them from log statements or mask and redact the data before it is written to logs."
            );

    private static final Pattern LOGGING_CALL =
            Pattern.compile(
                    "(?i)"
                            + "(?:"
                            + "\\b(?:log|logger)\\s*\\.\\s*"
                            + "(?:trace|debug|info|warn|error)"
                            + "\\s*\\("
                            + "|"
                            + "\\bSystem\\s*\\.\\s*(?:out|err)"
                            + "\\s*\\.\\s*(?:print|println|printf)"
                            + "\\s*\\("
                            + ")"
            );

    private static final Pattern SENSITIVE_IDENTIFIER =
            Pattern.compile(
                    "(?i)"
                            + "\\b(?:"
                            + "(?:get)?password"
                            + "|passwd"
                            + "|pwd"
                            + "|(?:access|refresh|auth)?[_-]?token"
                            + "|secret"
                            + "|api[_-]?key"
                            + "|authorization"
                            + "|credentials?"
                            + "|private[_-]?key"
                            + ")\\b"
            );

    private static final Pattern STRING_LITERAL =
            Pattern.compile(
                    "\"(?:\\\\.|[^\"\\\\])*\""
                            + "|"
                            + "'(?:\\\\.|[^'\\\\])*'"
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
                == SourceFileCategory.SOURCE_CODE;
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

            String line =
                    lines.get(index);

            String trimmed =
                    line.stripLeading();

            if (trimmed.startsWith("//")
                    || trimmed.startsWith("/*")
                    || trimmed.startsWith("*")) {
                continue;
            }

            if (!LOGGING_CALL
                    .matcher(line)
                    .find()) {
                continue;
            }

            String codeWithoutStrings =
                    STRING_LITERAL
                            .matcher(line)
                            .replaceAll("\"\"");

            if (!SENSITIVE_IDENTIFIER
                    .matcher(codeWithoutStrings)
                    .find()) {
                continue;
            }

            matches.add(
                    new RuleMatch(
                            METADATA.key(),
                            METADATA.defaultSeverity(),
                            file.normalizedPath(),
                            index + 1,
                            index + 1,
                            line.trim()
                    )
            );
        }

        return List.copyOf(matches);
    }
}