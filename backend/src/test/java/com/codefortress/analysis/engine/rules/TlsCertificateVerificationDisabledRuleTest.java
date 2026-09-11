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

class TlsCertificateVerificationDisabledRuleTest {

    private final TlsCertificateVerificationDisabledRule rule =
            new TlsCertificateVerificationDisabledRule();

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
                .isEqualTo("CF-SEC-003");

        assertThat(metadata.version())
                .isEqualTo("1.0.0");

        assertThat(metadata.title())
                .isEqualTo(
                        "TLS Certificate Verification Disabled"
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
                        "Client.java",
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
    void shouldDetectSslVerifyFalse() {
        List<RuleMatch> matches =
                rule.evaluate(
                        file(
                                "application.properties",
                                SourceFileCategory.CONFIGURATION,
                                """
                                ssl.verify=false
                                """
                        ),
                        context
                );

        assertThat(matches)
                .hasSize(1);

        RuleMatch match =
                matches.getFirst();

        assertThat(match.ruleKey())
                .isEqualTo("CF-SEC-003");

        assertThat(match.severity())
                .isEqualTo(Severity.HIGH);

        assertThat(match.startLine())
                .isEqualTo(1);

        assertThat(match.redactedEvidence())
                .contains(
                        "ssl.verify=false"
                );
    }

    @Test
    void shouldDetectRejectUnauthorizedFalse() {
        assertThat(
                rule.evaluate(
                        file(
                                "config.json",
                                SourceFileCategory.CONFIGURATION,
                                """
                                {
                                  "rejectUnauthorized": false
                                }
                                """
                        ),
                        context
                )
        ).hasSize(1);
    }

    @Test
    void shouldDetectInsecureSkipVerifyTrue() {
        List<RuleMatch> matches =
                rule.evaluate(
                        file(
                                "application.yml",
                                SourceFileCategory.CONFIGURATION,
                                """
                                insecureSkipVerify: true
                                """
                        ),
                        context
                );

        assertThat(matches)
                .hasSize(1);

        assertThat(
                matches.getFirst()
                        .startLine()
        ).isEqualTo(1);
    }

    @Test
    void shouldDetectEnvironmentStyleConfiguration() {
        assertThat(
                rule.evaluate(
                        file(
                                ".env",
                                SourceFileCategory.CONFIGURATION,
                                """
                                VERIFY_SSL=false
                                """
                        ),
                        context
                )
        ).hasSize(1);
    }

    @Test
    void shouldDetectAlternativeBooleanValues() {
        assertThat(
                rule.evaluate(
                        file(
                                "application.properties",
                                SourceFileCategory.CONFIGURATION,
                                """
                                tls.verify=off
                                """
                        ),
                        context
                )
        ).hasSize(1);

        assertThat(
                rule.evaluate(
                        file(
                                "application.yml",
                                SourceFileCategory.CONFIGURATION,
                                """
                                insecureSkipVerify: yes
                                """
                        ),
                        context
                )
        ).hasSize(1);
    }

    @Test
    void shouldIgnoreSecureConfiguration() {
        ScannableFile file =
                file(
                        "application.properties",
                        SourceFileCategory.CONFIGURATION,
                        """
                        ssl.verify=true
                        tls.verify=on
                        rejectUnauthorized=true
                        insecureSkipVerify=false
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
                        ssl.verify=${SSL_VERIFY}
                        insecureSkipVerify=${INSECURE_SKIP_VERIFY}
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
                        # ssl.verify=false
                        // rejectUnauthorized=false
                        database.verify=false
                        sslVerificationMode=false
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