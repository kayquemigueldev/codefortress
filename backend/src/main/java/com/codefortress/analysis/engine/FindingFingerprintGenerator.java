package com.codefortress.analysis.engine;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Objects;

public class FindingFingerprintGenerator {

    private static final String ALGORITHM =
            "SHA-256";

    public String generate(
            RuleMatch match
    ) {
        Objects.requireNonNull(
                match,
                "match must not be null"
        );

        String canonicalValue =
                canonicalValue(match);

        try {
            MessageDigest digest =
                    MessageDigest.getInstance(
                            ALGORITHM
                    );

            byte[] hash =
                    digest.digest(
                            canonicalValue.getBytes(
                                    StandardCharsets.UTF_8
                            )
                    );

            return HexFormat.of()
                    .formatHex(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "SHA-256 algorithm is not available",
                    exception
            );
        }
    }

    private String canonicalValue(
            RuleMatch match
    ) {
        return match.ruleKey()
                + "\n"
                + normalizePath(
                match.filePath()
        )
                + "\n"
                + normalizeEvidence(
                match.redactedEvidence()
        );
    }

    private String normalizePath(
            String path
    ) {
        return path
                .replace('\\', '/')
                .trim();
    }

    private String normalizeEvidence(
            String evidence
    ) {
        return evidence
                .replace("\r\n", "\n")
                .replace('\r', '\n')
                .trim();
    }
}