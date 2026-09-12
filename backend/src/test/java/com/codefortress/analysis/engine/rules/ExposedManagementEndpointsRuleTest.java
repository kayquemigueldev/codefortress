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

class ExposedManagementEndpointsRuleTest {

    private final ExposedManagementEndpointsRule rule =
            new ExposedManagementEndpointsRule();

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
                        "CF-SEC-008"
                );

        assertThat(metadata.version())
                .isEqualTo(
                        "1.0.0"
                );

        assertThat(metadata.title())
                .isEqualTo(
                        "Exposed Management Endpoints"
                );

        assertThat(metadata.category())
                .isEqualTo(
                        FindingCategory.CONFIGURATION
                );

        assertThat(metadata.defaultSeverity())
                .isEqualTo(
                        Severity.HIGH
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
                        "Application.java",
                        SourceFileCategory.SOURCE_CODE,
                        ""
                ))
        ).isFalse();

        assertThat(
                rule.supports(file(
                        "pom.xml",
                        SourceFileCategory.DEPENDENCY_MANIFEST,
                        ""
                ))
        ).isFalse();
    }

    @Test
    void shouldDetectWildcardManagementEndpointExposure() {
        List<RuleMatch> matches =
                rule.evaluate(
                        file(
                                "application.properties",
                                SourceFileCategory.CONFIGURATION,
                                """
                                management.endpoints.web.exposure.include=*
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
                        "CF-SEC-008"
                );

        assertThat(match.severity())
                .isEqualTo(
                        Severity.HIGH
                );

        assertThat(match.startLine())
                .isEqualTo(1);

        assertThat(match.endLine())
                .isEqualTo(1);

        assertThat(match.redactedEvidence())
                .contains(
                        "management.endpoints.web.exposure.include=*"
                );
    }

    @Test
    void shouldDetectEnvironmentStyleConfiguration() {
        List<RuleMatch> matches =
                rule.evaluate(
                        file(
                                ".env",
                                SourceFileCategory.CONFIGURATION,
                                """
                                MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=*
                                """
                        ),
                        context
                );

        assertThat(matches)
                .hasSize(1);
    }

    @Test
    void shouldDetectNestedYamlWildcardExposure() {
        List<RuleMatch> matches =
                rule.evaluate(
                        file(
                                "application.yml",
                                SourceFileCategory.CONFIGURATION,
                                """
                                management:
                                  endpoints:
                                    web:
                                      exposure:
                                        include: "*"
                                """
                        ),
                        context
                );

        assertThat(matches)
                .hasSize(1);

        RuleMatch match =
                matches.getFirst();

        assertThat(match.startLine())
                .isEqualTo(5);

        assertThat(match.redactedEvidence())
                .contains(
                        "include: \"*\""
                );
    }

    @Test
    void shouldDetectWildcardInsideYamlList() {
        List<RuleMatch> matches =
                rule.evaluate(
                        file(
                                "application.yml",
                                SourceFileCategory.CONFIGURATION,
                                """
                                management:
                                  endpoints:
                                    web:
                                      exposure:
                                        include: [health, info, "*"]
                                """
                        ),
                        context
                );

        assertThat(matches)
                .hasSize(1);
    }

    @Test
    void shouldDetectWildcardInsideEndpointList() {
        List<RuleMatch> matches =
                rule.evaluate(
                        file(
                                "application.properties",
                                SourceFileCategory.CONFIGURATION,
                                """
                                management.endpoints.web.exposure.include=health,info,*
                                """
                        ),
                        context
                );

        assertThat(matches)
                .hasSize(1);
    }

    @Test
    void shouldIgnoreExplicitManagementEndpoints() {
        ScannableFile file =
                file(
                        "application.properties",
                        SourceFileCategory.CONFIGURATION,
                        """
                        management.endpoints.web.exposure.include=health,info
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
    void shouldIgnorePlaceholderConfiguration() {
        ScannableFile file =
                file(
                        "application.properties",
                        SourceFileCategory.CONFIGURATION,
                        """
                        management.endpoints.web.exposure.include=${MANAGEMENT_ENDPOINTS}
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
                        # management.endpoints.web.exposure.include=*
                        // management.endpoints.web.exposure.include=*
                        ; MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=*
                        application.features.include=*
                        management.server.port=8081
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
    void shouldIgnoreWildcardIncludeFromUnrelatedYamlPath() {
        ScannableFile file =
                file(
                        "application.yml",
                        SourceFileCategory.CONFIGURATION,
                        """
                        application:
                          endpoints:
                            web:
                              exposure:
                                include: "*"
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
    void shouldIgnoreSourceCodeFiles() {
        ScannableFile file =
                file(
                        "Application.java",
                        SourceFileCategory.SOURCE_CODE,
                        """
                        String config =
                                "management.endpoints.web.exposure.include=*";
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