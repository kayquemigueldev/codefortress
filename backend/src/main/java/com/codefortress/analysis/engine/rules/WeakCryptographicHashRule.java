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

public class WeakCryptographicHashRule
        implements SecurityRule {

    private static final RuleMetadata METADATA =
            new RuleMetadata(
                    "CF-SEC-006",
                    "1.0.0",
                    "Weak Cryptographic Hash Algorithm",
                    FindingCategory.CODE,
                    Severity.MEDIUM,
                    "Detects the use of cryptographically weak hash algorithms such as MD5 and SHA-1.",
                    "Weak hash algorithms are vulnerable to collision attacks and should not be used for security-sensitive hashing, signatures, integrity checks, or credential protection.",
                    "Use a modern cryptographic hash algorithm such as SHA-256 or SHA-512. For password storage, use a dedicated password hashing algorithm such as Argon2, bcrypt, or scrypt."
            );

    private static final Pattern WEAK_MESSAGE_DIGEST =
            Pattern.compile(
                    "(?i)"
                            + "MessageDigest"
                            + "\\s*\\.\\s*"
                            + "getInstance"
                            + "\\s*\\(\\s*"
                            + "[\"']"
                            + "\\s*"
                            + "(MD2|MD5|SHA-?1)"
                            + "\\s*"
                            + "[\"']"
                            + "\\s*\\)"
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
                    WEAK_MESSAGE_DIGEST.matcher(line);

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