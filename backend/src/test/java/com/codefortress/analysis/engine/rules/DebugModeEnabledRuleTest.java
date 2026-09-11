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

class DebugModeEnabledRuleTest {

    private final DebugModeEnabledRule rule =
            new DebugModeEnabledRule();

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
                .isEqualTo("CF-SEC-002");

        assertThat(metadata.version())
                .isEqualTo("1.0.0");

        assertThat(metadata.title())
                .isEqualTo(
                        "Debug Mode Enabled"
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
    void shouldDetectDebugEnabledInProperties() {
        ScannableFile file =
                file(
                        "application.properties",
                        SourceFileCategory.CONFIGURATION,
                        """
                        app.name=codefortress
                        debug=true
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
                .isEqualTo("CF-SEC-002");

        assertThat(match.severity())
                .isEqualTo(Severity.MEDIUM);

        assertThat(match.startLine())
                .isEqualTo(2);

        assertThat(match.filePath())
                .isEqualTo(
                        "application.properties"
                );

        assertThat(match.redactedEvidence())
                .contains("debug=true");
    }

    @Test
    void shouldDetectEnvironmentDebugFlag() {
        ScannableFile file =
                file(
                        ".env",
                        SourceFileCategory.CONFIGURATION,
                        """
                        APP_NAME=CodeFortress
                        APP_DEBUG=true
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
    void shouldDetectNumericDebugFlag() {
        ScannableFile file =
                file(
                        ".env",
                        SourceFileCategory.CONFIGURATION,
                        """
                        FLASK_DEBUG=1
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
    void shouldDetectYamlDebugFlag() {
        ScannableFile file =
                file(
                        "application.yml",
                        SourceFileCategory.CONFIGURATION,
                        """
                        spring.debug: true
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
    void shouldDetectJsonDebugFlag() {
        ScannableFile file =
                file(
                        "config.json",
                        SourceFileCategory.CONFIGURATION,
                        """
                        {
                          "debug": true,
                          "name": "CodeFortress"
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

        assertThat(
                matches.getFirst()
                        .startLine()
        ).isEqualTo(2);
    }

    @Test
    void shouldIgnoreDisabledDebugValues() {
        ScannableFile file =
                file(
                        "application.properties",
                        SourceFileCategory.CONFIGURATION,
                        """
                        debug=false
                        APP_DEBUG=0
                        FLASK_DEBUG=off
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
    void shouldIgnorePlaceholder() {
        ScannableFile file =
                file(
                        "application.properties",
                        SourceFileCategory.CONFIGURATION,
                        """
                        debug=${APP_DEBUG}
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
                        # debug=true
                        // debug=true
                        debugToolbar=true
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