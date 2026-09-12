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

class SqlInjectionRiskRuleTest {

    private final SqlInjectionRiskRule rule =
            new SqlInjectionRiskRule();

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
                .isEqualTo("CF-SEC-005");

        assertThat(metadata.version())
                .isEqualTo("1.0.0");

        assertThat(metadata.title())
                .isEqualTo(
                        "SQL Injection Risk"
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
                rule.supports(file(
                        "UserRepository.java",
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
    void shouldDetectSelectConcatenation() {
        ScannableFile file = file(
                "src/UserRepository.java",
                SourceFileCategory.SOURCE_CODE,
                """
                class UserRepository {
                    String sql = "SELECT * FROM users WHERE id = " + userId;
                }
                """
        );

        List<RuleMatch> matches =
                rule.evaluate(file, context);

        assertThat(matches)
                .hasSize(1);

        RuleMatch match =
                matches.getFirst();

        assertThat(match.ruleKey())
                .isEqualTo("CF-SEC-005");

        assertThat(match.severity())
                .isEqualTo(Severity.HIGH);

        assertThat(match.filePath())
                .isEqualTo(
                        "src/UserRepository.java"
                );

        assertThat(match.startLine())
                .isEqualTo(2);

        assertThat(match.endLine())
                .isEqualTo(2);

        assertThat(match.redactedEvidence())
                .contains("SELECT");
    }

    @Test
    void shouldDetectDeleteConcatenation() {
        ScannableFile file = file(
                "src/UserRepository.java",
                SourceFileCategory.SOURCE_CODE,
                """
                class UserRepository {
                    String sql = "DELETE FROM users WHERE id = " + userId;
                }
                """
        );

        assertThat(
                rule.evaluate(file, context)
        ).hasSize(1);
    }

    @Test
    void shouldDetectUpdateConcatenation() {
        ScannableFile file = file(
                "src/UserRepository.java",
                SourceFileCategory.SOURCE_CODE,
                """
                class UserRepository {
                    String sql = "UPDATE users SET active = true WHERE id = " + userId;
                }
                """
        );

        assertThat(
                rule.evaluate(file, context)
        ).hasSize(1);
    }

    @Test
    void shouldDetectInsertConcatenation() {
        ScannableFile file = file(
                "src/UserRepository.java",
                SourceFileCategory.SOURCE_CODE,
                """
                class UserRepository {
                    String sql = "INSERT INTO users VALUES (" + values;
                }
                """
        );

        assertThat(
                rule.evaluate(file, context)
        ).hasSize(1);
    }

    @Test
    void shouldIgnoreParameterizedQuery() {
        ScannableFile file = file(
                "src/UserRepository.java",
                SourceFileCategory.SOURCE_CODE,
                """
                class UserRepository {
                    String sql = "SELECT * FROM users WHERE id = ?";
                }
                """
        );

        assertThat(
                rule.evaluate(file, context)
        ).isEmpty();
    }

    @Test
    void shouldIgnoreStaticSqlStatement() {
        ScannableFile file = file(
                "src/UserRepository.java",
                SourceFileCategory.SOURCE_CODE,
                """
                class UserRepository {
                    String sql = "SELECT * FROM users";
                }
                """
        );

        assertThat(
                rule.evaluate(file, context)
        ).isEmpty();
    }

    @Test
    void shouldIgnoreConfigurationFile() {
        ScannableFile file = file(
                "application.properties",
                SourceFileCategory.CONFIGURATION,
                """
                query=SELECT * FROM users
                """
        );

        assertThat(
                rule.evaluate(file, context)
        ).isEmpty();
    }

    @Test
    void shouldDetectMultipleUnsafeQueries() {
        ScannableFile file = file(
                "src/UserRepository.java",
                SourceFileCategory.SOURCE_CODE,
                """
                class UserRepository {
                    String select = "SELECT * FROM users WHERE id = " + userId;
                    String delete = "DELETE FROM users WHERE id = " + userId;
                }
                """
        );

        List<RuleMatch> matches =
                rule.evaluate(file, context);

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