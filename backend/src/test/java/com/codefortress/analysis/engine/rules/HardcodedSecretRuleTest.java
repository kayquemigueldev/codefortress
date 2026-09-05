package com.codefortress.analysis.engine.rules;

import com.codefortress.analysis.Severity;
import com.codefortress.analysis.discovery.SourceFileCategory;
import com.codefortress.analysis.engine.AnalysisContext;
import com.codefortress.analysis.engine.RuleMatch;
import com.codefortress.analysis.engine.ScannableFile;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class HardcodedSecretRuleTest {

    private final HardcodedSecretRule rule =
            new HardcodedSecretRule();

    private final AnalysisContext context =
            new AnalysisContext(
                    UUID.randomUUID(),
                    "ruleset-java-v1"
            );

    @Test
    void shouldExposeRuleIdentity() {
        assertThat(rule.key())
                .isEqualTo("CF-SEC-001");

        assertThat(rule.version())
                .isEqualTo("1.0.0");
    }

    @Test
    void shouldSupportSourceCodeAndConfiguration() {
        assertThat(
                rule.supports(file(
                        "Config.java",
                        SourceFileCategory.SOURCE_CODE,
                        ""
                ))
        ).isTrue();

        assertThat(
                rule.supports(file(
                        "application.properties",
                        SourceFileCategory.CONFIGURATION,
                        ""
                ))
        ).isTrue();

        assertThat(
                rule.supports(file(
                        "pom.xml",
                        SourceFileCategory.DEPENDENCY_MANIFEST,
                        ""
                ))
        ).isFalse();
    }

    @Test
    void shouldDetectHardcodedPassword() {
        ScannableFile file = file(
                "src/main/java/Config.java",
                SourceFileCategory.SOURCE_CODE,
                """
                class Config {
                    String password = "super-secret-password";
                }
                """
        );

        List<RuleMatch> matches =
                rule.evaluate(file, context);

        assertThat(matches)
                .hasSize(1);

        RuleMatch match = matches.getFirst();

        assertThat(match.ruleKey())
                .isEqualTo("CF-SEC-001");

        assertThat(match.severity())
                .isEqualTo(Severity.CRITICAL);

        assertThat(match.filePath())
                .isEqualTo(
                        "src/main/java/Config.java"
                );

        assertThat(match.startLine())
                .isEqualTo(2);

        assertThat(match.endLine())
                .isEqualTo(2);

        assertThat(match.redactedEvidence())
                .contains("password");

        assertThat(match.redactedEvidence())
                .doesNotContain(
                        "super-secret-password"
                );
    }

    @Test
    void shouldDetectConfigurationPassword() {
        ScannableFile file = file(
                "application.properties",
                SourceFileCategory.CONFIGURATION,
                """
                spring.datasource.url=jdbc:postgresql://localhost/app
                spring.datasource.password=super-secret-password
                """
        );

        List<RuleMatch> matches =
                rule.evaluate(file, context);

        assertThat(matches)
                .hasSize(1);

        RuleMatch match = matches.getFirst();

        assertThat(match.startLine())
                .isEqualTo(2);

        assertThat(match.redactedEvidence())
                .contains(
                        "spring.datasource.password"
                );

        assertThat(match.redactedEvidence())
                .doesNotContain(
                        "super-secret-password"
                );
    }

    @Test
    void shouldDetectDottedApiKeyInConfiguration() {
        ScannableFile file = file(
                "application.properties",
                SourceFileCategory.CONFIGURATION,
                """
                api.key=secret-api-key
                """
        );

        List<RuleMatch> matches =
                rule.evaluate(file, context);

        assertThat(matches)
                .hasSize(1);

        RuleMatch match =
                matches.getFirst();

        assertThat(match.severity())
                .isEqualTo(
                        Severity.CRITICAL
                );

        assertThat(match.redactedEvidence())
                .contains("api.key");

        assertThat(match.redactedEvidence())
                .contains("********");

        assertThat(match.redactedEvidence())
                .doesNotContain(
                        "secret-api-key"
                );
    }

    @Test
    void shouldDetectMultipleHardcodedSecrets() {
        ScannableFile file = file(
                "Config.java",
                SourceFileCategory.SOURCE_CODE,
                """
                class Config {
                    String password = "super-secret-password";
                    String apiKey = "sk_live_example_value";
                }
                """
        );

        List<RuleMatch> matches =
                rule.evaluate(file, context);

        assertThat(matches)
                .hasSize(2);

        assertThat(matches)
                .extracting(RuleMatch::startLine)
                .containsExactly(2, 3);
    }

    @Test
    void shouldIgnoreEnvironmentVariable() {
        ScannableFile file = file(
                "Config.java",
                SourceFileCategory.SOURCE_CODE,
                """
                class Config {
                    String password =
                            System.getenv("DB_PASSWORD");
                }
                """
        );

        assertThat(
                rule.evaluate(file, context)
        ).isEmpty();
    }

    @Test
    void shouldIgnorePlaceholder() {
        ScannableFile file = file(
                "application.properties",
                SourceFileCategory.CONFIGURATION,
                """
                spring.datasource.password=${DB_PASSWORD}
                api.key=${API_KEY}
                """
        );

        assertThat(
                rule.evaluate(file, context)
        ).isEmpty();
    }

    @Test
    void shouldIgnoreEmptySecret() {
        ScannableFile file = file(
                "Config.java",
                SourceFileCategory.SOURCE_CODE,
                """
                class Config {
                    String password = "";
                }
                """
        );

        assertThat(
                rule.evaluate(file, context)
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