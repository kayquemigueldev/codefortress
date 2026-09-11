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

class PermissiveCorsConfigurationRuleTest {

    private final PermissiveCorsConfigurationRule rule =
            new PermissiveCorsConfigurationRule();

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
                        "CF-SEC-004"
                );

        assertThat(metadata.version())
                .isEqualTo(
                        "1.0.0"
                );

        assertThat(metadata.title())
                .isEqualTo(
                        "Permissive CORS Configuration"
                );

        assertThat(metadata.category())
                .isEqualTo(
                        FindingCategory.CONFIGURATION
                );

        assertThat(metadata.defaultSeverity())
                .isEqualTo(
                        Severity.MEDIUM
                );
    }

    @Test
    void shouldSupportOnlyConfigurationFiles() {
        assertThat(
                rule.supports(file(
                        "application.properties",
                        SourceFileCategory.CONFIGURATION,
                        ""
                ))
        ).isTrue();

        assertThat(
                rule.supports(file(
                        "SecurityConfig.java",
                        SourceFileCategory.SOURCE_CODE,
                        ""
                ))
        ).isFalse();

        assertThat(
                rule.supports(file(
                        "package.json",
                        SourceFileCategory.DEPENDENCY_MANIFEST,
                        ""
                ))
        ).isFalse();
    }

    @Test
    void shouldDetectWildcardAllowedOrigins() {
        List<RuleMatch> matches =
                rule.evaluate(
                        file(
                                "application.properties",
                                SourceFileCategory.CONFIGURATION,
                                """
                                cors.allowed-origins=*
                                """
                        ),
                        context
                );

        assertThat(matches)
                .hasSize(1);

        RuleMatch match =
                matches.getFirst();

        assertThat(match.ruleKey())
                .isEqualTo(
                        "CF-SEC-004"
                );

        assertThat(match.severity())
                .isEqualTo(
                        Severity.MEDIUM
                );

        assertThat(match.startLine())
                .isEqualTo(1);

        assertThat(match.redactedEvidence())
                .contains(
                        "cors.allowed-origins=*"
                );
    }

    @Test
    void shouldDetectCamelCaseAllowedOrigins() {
        assertThat(
                rule.evaluate(
                        file(
                                "config.properties",
                                SourceFileCategory.CONFIGURATION,
                                """
                                allowedOrigins=*
                                """
                        ),
                        context
                )
        ).hasSize(1);
    }

    @Test
    void shouldDetectYamlWildcardOrigin() {
        assertThat(
                rule.evaluate(
                        file(
                                "application.yml",
                                SourceFileCategory.CONFIGURATION,
                                """
                                cors:
                                  allowed-origins: "*"
                                """
                        ),
                        context
                )
        ).hasSize(1);
    }

    @Test
    void shouldDetectEnvironmentStyleConfiguration() {
        assertThat(
                rule.evaluate(
                        file(
                                ".env",
                                SourceFileCategory.CONFIGURATION,
                                """
                                CORS_ALLOWED_ORIGINS=*
                                """
                        ),
                        context
                )
        ).hasSize(1);
    }

    @Test
    void shouldDetectWildcardInsideOriginList() {
        assertThat(
                rule.evaluate(
                        file(
                                "application.properties",
                                SourceFileCategory.CONFIGURATION,
                                """
                                cors.allowed-origins=https://app.example.com,*
                                """
                        ),
                        context
                )
        ).hasSize(1);
    }

    @Test
    void shouldIgnoreExplicitTrustedOrigins() {
        ScannableFile file =
                file(
                        "application.properties",
                        SourceFileCategory.CONFIGURATION,
                        """
                        cors.allowed-origins=https://app.example.com
                        cors.origin=https://admin.example.com
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
    void shouldIgnorePlaceholderValues() {
        ScannableFile file =
                file(
                        "application.properties",
                        SourceFileCategory.CONFIGURATION,
                        """
                        cors.allowed-origins=${CORS_ALLOWED_ORIGINS}
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
    void shouldIgnoreCommentsAndUnrelatedKeys() {
        ScannableFile file =
                file(
                        "application.properties",
                        SourceFileCategory.CONFIGURATION,
                        """
                        # cors.allowed-origins=*
                        // allowedOrigins=*
                        database.allowed-origins=*
                        storage.origin=*
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