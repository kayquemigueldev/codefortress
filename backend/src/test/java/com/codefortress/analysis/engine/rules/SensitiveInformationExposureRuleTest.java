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

class SensitiveInformationExposureRuleTest {

    private final SensitiveInformationExposureRule rule =
            new SensitiveInformationExposureRule();

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
                .isEqualTo("CF-SEC-007");

        assertThat(metadata.version())
                .isEqualTo("1.0.0");

        assertThat(metadata.title())
                .isEqualTo(
                        "Sensitive Information Exposure"
                );

        assertThat(metadata.category())
                .isEqualTo(
                        FindingCategory.CODE
                );

        assertThat(metadata.defaultSeverity())
                .isEqualTo(
                        Severity.HIGH
                );
    }

    @Test
    void shouldSupportSourceCodeOnly() {
        assertThat(
                rule.supports(
                        file(
                                "UserService.java",
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
    }

    @Test
    void shouldDetectPasswordLoggedWithLogger() {
        ScannableFile file =
                file(
                        "src/UserService.java",
                        SourceFileCategory.SOURCE_CODE,
                        """
                        class UserService {
                            void login(String password) {
                                log.info("Password: {}", password);
                            }
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
                        "CF-SEC-007"
                );

        assertThat(match.severity())
                .isEqualTo(
                        Severity.HIGH
                );

        assertThat(match.filePath())
                .isEqualTo(
                        "src/UserService.java"
                );

        assertThat(match.startLine())
                .isEqualTo(3);

        assertThat(match.endLine())
                .isEqualTo(3);

        assertThat(match.redactedEvidence())
                .contains(
                        "password"
                );
    }

    @Test
    void shouldDetectAccessTokenConcatenatedIntoLog() {
        ScannableFile file =
                file(
                        "src/AuthService.java",
                        SourceFileCategory.SOURCE_CODE,
                        """
                        class AuthService {
                            void authenticate(String accessToken) {
                                logger.debug("Token: " + accessToken);
                            }
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
    void shouldDetectSecretPrintedToStandardOutput() {
        ScannableFile file =
                file(
                        "src/App.java",
                        SourceFileCategory.SOURCE_CODE,
                        """
                        class App {
                            void run(String secret) {
                                System.out.println("Secret: " + secret);
                            }
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
    void shouldDetectApiKeyPrintedToStandardError() {
        ScannableFile file =
                file(
                        "src/App.java",
                        SourceFileCategory.SOURCE_CODE,
                        """
                        class App {
                            void run(String apiKey) {
                                System.err.println("API key: " + apiKey);
                            }
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
    void shouldIgnoreNormalLogMessages() {
        ScannableFile file =
                file(
                        "src/UserService.java",
                        SourceFileCategory.SOURCE_CODE,
                        """
                        class UserService {
                            void login() {
                                log.info("User authenticated successfully");
                            }
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
    void shouldIgnoreSensitiveWordInsideLogMessageOnly() {
        ScannableFile file =
                file(
                        "src/UserService.java",
                        SourceFileCategory.SOURCE_CODE,
                        """
                        class UserService {
                            void resetPassword() {
                                log.info("Password reset requested");
                            }
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
    void shouldIgnoreSensitiveVariableWhenNotLogged() {
        ScannableFile file =
                file(
                        "src/UserService.java",
                        SourceFileCategory.SOURCE_CODE,
                        """
                        class UserService {
                            void login(String password) {
                                String value = password;
                            }
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
    void shouldIgnoreCommentedLoggingStatement() {
        ScannableFile file =
                file(
                        "src/UserService.java",
                        SourceFileCategory.SOURCE_CODE,
                        """
                        class UserService {
                            void login(String password) {
                                // log.info("Password: {}", password);
                            }
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
                        password=example
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
    void shouldDetectMultipleSensitiveLogStatements() {
        ScannableFile file =
                file(
                        "src/AuthService.java",
                        SourceFileCategory.SOURCE_CODE,
                        """
                        class AuthService {
                            void authenticate(String password, String accessToken) {
                                log.info("Password: {}", password);
                                log.debug("Token: {}", accessToken);
                            }
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
                        3,
                        4
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