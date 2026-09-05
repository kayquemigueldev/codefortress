package com.codefortress.analysis.engine;

import com.codefortress.analysis.discovery.SourceFileCategory;
import com.codefortress.analysis.engine.rules.HardcodedSecretRule;
import com.codefortress.analysis.FindingCategory;
import com.codefortress.analysis.Severity;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SecurityRuleExecutorTest {

    private final AnalysisContext context =
            new AnalysisContext(
                    UUID.randomUUID(),
                    "ruleset-java-v1"
            );

    @Test
    void shouldExecuteRulesAcrossFiles() {
        SecurityRuleExecutor executor =
                new SecurityRuleExecutor(
                        List.of(
                                new HardcodedSecretRule()
                        )
                );

        ScannableFile sourceFile =
                file(
                        "src/main/java/Config.java",
                        SourceFileCategory.SOURCE_CODE,
                        """
                        class Config {
                            String password = "secret-password";
                        }
                        """
                );

        ScannableFile configurationFile =
                file(
                        "application.properties",
                        SourceFileCategory.CONFIGURATION,
                        """
                        api.key=secret-api-key
                        """
                );

        List<RuleMatch> matches =
                executor.execute(
                        List.of(
                                sourceFile,
                                configurationFile
                        ),
                        context
                );

        assertThat(matches)
                .hasSize(2);

        assertThat(matches)
                .extracting(
                        RuleMatch::filePath
                )
                .containsExactly(
                        "application.properties",
                        "src/main/java/Config.java"
                );
    }

    @Test
    void shouldSkipUnsupportedRule() {
        SecurityRule unsupportedRule =
                new SecurityRule() {

                    @Override
                    public RuleMetadata metadata() {
                        return new RuleMetadata(
                                "CF-TEST-001",
                                "1.0.0",
                                "Unsupported Test Rule",
                                FindingCategory.CODE,
                                Severity.LOW,
                                "Rule used to verify unsupported files are skipped.",
                                "No production impact.",
                                "No remediation is required."
                        );
                    }

                    @Override
                    public boolean supports(
                            ScannableFile file
                    ) {
                        return false;
                    }

                    @Override
                    public List<RuleMatch> evaluate(
                            ScannableFile file,
                            AnalysisContext context
                    ) {
                        throw new AssertionError(
                                "evaluate must not be called"
                        );
                    }
                };

        SecurityRuleExecutor executor =
                new SecurityRuleExecutor(
                        List.of(
                                unsupportedRule
                        )
                );

        List<RuleMatch> matches =
                executor.execute(
                        List.of(
                                file(
                                        "pom.xml",
                                        SourceFileCategory
                                                .DEPENDENCY_MANIFEST,
                                        "<project />"
                                )
                        ),
                        context
                );

        assertThat(matches)
                .isEmpty();
    }

    @Test
    void shouldReturnImmutableMatches() {
        SecurityRuleExecutor executor =
                new SecurityRuleExecutor(
                        List.of(
                                new HardcodedSecretRule()
                        )
                );

        List<RuleMatch> matches =
                executor.execute(
                        List.of(
                                file(
                                        "Config.java",
                                        SourceFileCategory.SOURCE_CODE,
                                        """
                                        class Config {
                                            String password = "secret-password";
                                        }
                                        """
                                )
                        ),
                        context
                );

        assertThatThrownBy(() ->
                matches.add(null)
        )
                .isInstanceOf(
                        UnsupportedOperationException.class
                );
    }

    @Test
    void shouldRejectMissingContext() {
        SecurityRuleExecutor executor =
                new SecurityRuleExecutor(
                        List.of(
                                new HardcodedSecretRule()
                        )
                );

        assertThatThrownBy(() ->
                executor.execute(
                        List.of(),
                        null
                )
        )
                .isInstanceOf(
                        NullPointerException.class
                )
                .hasMessage(
                        "context must not be null"
                );
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