package com.codefortress.analysis.engine.rules;

import com.codefortress.analysis.FindingCategory;
import com.codefortress.analysis.Severity;
import com.codefortress.analysis.discovery.SourceFileCategory;
import com.codefortress.analysis.engine.AnalysisContext;
import com.codefortress.analysis.engine.RuleMatch;
import com.codefortress.analysis.engine.RuleMetadata;
import com.codefortress.analysis.engine.ScannableFile;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class WeakCryptographicHashRuleTest {

    private final WeakCryptographicHashRule rule =
            new WeakCryptographicHashRule();

    private final AnalysisContext context =
            new AnalysisContext(
                    UUID.randomUUID(),
                    "ruleset-java-v1"
            );

    @Test
    void shouldExposeRuleMetadata() {
        RuleMetadata metadata =
                rule.metadata();

        assertThat(metadata.key())
                .isEqualTo(
                        "CF-SEC-006"
                );

        assertThat(metadata.version())
                .isEqualTo(
                        "1.0.0"
                );

        assertThat(metadata.title())
                .isEqualTo(
                        "Weak Cryptographic Hash Algorithm"
                );

        assertThat(metadata.category())
                .isEqualTo(
                        FindingCategory.CODE
                );

        assertThat(metadata.defaultSeverity())
                .isEqualTo(
                        Severity.MEDIUM
                );
    }

    @Test
    void shouldSupportSourceCodeOnly() {
        assertThat(
                rule.supports(
                        file(
                                "PasswordService.java",
                                SourceFileCategory.SOURCE_CODE,
                                ""
                        )
                )
        ).isTrue();

        assertThat(
                rule.supports(
                        file(
                                "application.properties",
                                SourceFileCategory.CONFIGURATION,
                                ""
                        )
                )
        ).isFalse();

        assertThat(
                rule.supports(
                        file(
                                "pom.xml",
                                SourceFileCategory.DEPENDENCY_MANIFEST,
                                ""
                        )
                )
        ).isFalse();
    }

    @Test
    void shouldDetectMd5MessageDigest() {
        ScannableFile file =
                file(
                        "src/HashService.java",
                        SourceFileCategory.SOURCE_CODE,
                        """
                        class HashService {
                            MessageDigest digest = MessageDigest.getInstance("MD5");
                        }
                        """
                );

        List<RuleMatch> matches =
                rule.evaluate(
                        file,
                        context
                );

        assertThat(matches)
                .hasSize(1);

        RuleMatch match =
                matches.getFirst();

        assertThat(match.ruleKey())
                .isEqualTo(
                        "CF-SEC-006"
                );

        assertThat(match.severity())
                .isEqualTo(
                        Severity.MEDIUM
                );

        assertThat(match.filePath())
                .isEqualTo(
                        "src/HashService.java"
                );

        assertThat(match.startLine())
                .isEqualTo(2);

        assertThat(match.endLine())
                .isEqualTo(2);

        assertThat(match.redactedEvidence())
                .contains(
                        "MD5"
                );
    }

    @Test
    void shouldDetectSha1WithHyphen() {
        ScannableFile file =
                file(
                        "src/HashService.java",
                        SourceFileCategory.SOURCE_CODE,
                        """
                        class HashService {
                            MessageDigest digest = MessageDigest.getInstance("SHA-1");
                        }
                        """
                );

        assertThat(
                rule.evaluate(
                        file,
                        context
                )
        ).hasSize(1);
    }

    @Test
    void shouldDetectSha1WithoutHyphen() {
        ScannableFile file =
                file(
                        "src/HashService.java",
                        SourceFileCategory.SOURCE_CODE,
                        """
                        class HashService {
                            MessageDigest digest = MessageDigest.getInstance("SHA1");
                        }
                        """
                );

        assertThat(
                rule.evaluate(
                        file,
                        context
                )
        ).hasSize(1);
    }

    @Test
    void shouldDetectFullyQualifiedMessageDigest() {
        ScannableFile file =
                file(
                        "src/HashService.java",
                        SourceFileCategory.SOURCE_CODE,
                        """
                        class HashService {
                            var digest = java.security.MessageDigest.getInstance("MD5");
                        }
                        """
                );

        assertThat(
                rule.evaluate(
                        file,
                        context
                )
        ).hasSize(1);
    }

    @Test
    void shouldDetectMultipleWeakAlgorithms() {
        ScannableFile file =
                file(
                        "src/HashService.java",
                        SourceFileCategory.SOURCE_CODE,
                        """
                        class HashService {
                            var first = MessageDigest.getInstance("MD5");
                            var second = MessageDigest.getInstance("SHA-1");
                        }
                        """
                );

        List<RuleMatch> matches =
                rule.evaluate(
                        file,
                        context
                );

        assertThat(matches)
                .hasSize(2);

        assertThat(matches)
                .extracting(
                        RuleMatch::startLine
                )
                .containsExactly(
                        2,
                        3
                );
    }

    @Test
    void shouldIgnoreModernHashAlgorithms() {
        ScannableFile file =
                file(
                        "src/HashService.java",
                        SourceFileCategory.SOURCE_CODE,
                        """
                        class HashService {
                            var first = MessageDigest.getInstance("SHA-256");
                            var second = MessageDigest.getInstance("SHA-512");
                        }
                        """
                );

        assertThat(
                rule.evaluate(
                        file,
                        context
                )
        ).isEmpty();
    }

    @Test
    void shouldIgnorePlainTextMentionOfWeakAlgorithm() {
        ScannableFile file =
                file(
                        "src/HashService.java",
                        SourceFileCategory.SOURCE_CODE,
                        """
                        class HashService {
                            String warning = "MD5 and SHA-1 are deprecated";
                        }
                        """
                );

        assertThat(
                rule.evaluate(
                        file,
                        context
                )
        ).isEmpty();
    }

    @Test
    void shouldIgnoreConfigurationFiles() {
        ScannableFile file =
                file(
                        "application.properties",
                        SourceFileCategory.CONFIGURATION,
                        """
                        hashing.algorithm=MD5
                        """
                );

        assertThat(
                rule.evaluate(
                        file,
                        context
                )
        ).isEmpty();
    }

    private ScannableFile file(
            String path,
            SourceFileCategory category,
            String content
    ) {
        return new ScannableFile(
                path,
                category,
                content,
                Math.toIntExact(
                        content.lines().count()
                )
        );
    }
}