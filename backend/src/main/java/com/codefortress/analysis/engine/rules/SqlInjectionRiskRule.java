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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SqlInjectionRiskRule
        implements SecurityRule {

    private static final RuleMetadata METADATA =
            new RuleMetadata(
                    "CF-SEC-005",
                    "1.0.0",
                    "SQL Injection Risk",
                    FindingCategory.CODE,
                    Severity.HIGH,
                    "Detects SQL statements constructed through direct string concatenation with variable data.",
                    "Building SQL queries with untrusted values may allow attackers to alter the intended query and access, modify, or delete sensitive data.",
                    "Use parameterized queries, prepared statements, or ORM query parameters instead of concatenating values directly into SQL statements."
            );

    private static final Pattern CONCATENATED_SQL =
            Pattern.compile(
                    "(?i)"
                            + "([\"'])"
                            + "\\s*"
                            + "(select|insert|update|delete)"
                            + "\\b"
                            + "[^\"'\\r\\n]*"
                            + "\\1"
                            + "\\s*\\+\\s*"
                            + "[a-zA-Z_$][a-zA-Z0-9_$.\\[\\]()]"
                            + "*"
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

            String line = lines.get(index);

            Matcher matcher =
                    CONCATENATED_SQL.matcher(line);

            if (!matcher.find()) {
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