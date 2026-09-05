package com.codefortress.analysis.engine;

import com.codefortress.analysis.Severity;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FindingFingerprintGeneratorTest {

    private final FindingFingerprintGenerator generator =
            new FindingFingerprintGenerator();

    @Test
    void shouldGenerateSha256Fingerprint() {
        String fingerprint =
                generator.generate(
                        match(
                                "src/main/java/Config.java",
                                "String password = \"********\";"
                        )
                );

        assertThat(fingerprint)
                .hasSize(64);

        assertThat(fingerprint)
                .matches("[0-9a-f]{64}");
    }

    @Test
    void shouldGenerateSameFingerprintForSameFinding() {
        RuleMatch first =
                match(
                        "src/main/java/Config.java",
                        "String password = \"********\";"
                );

        RuleMatch second =
                match(
                        "src/main/java/Config.java",
                        "String password = \"********\";"
                );

        assertThat(
                generator.generate(first)
        ).isEqualTo(
                generator.generate(second)
        );
    }

    @Test
    void shouldNormalizePathSeparators() {
        RuleMatch unixPath =
                match(
                        "src/main/java/Config.java",
                        "password=********"
                );

        RuleMatch windowsPath =
                match(
                        "src\\main\\java\\Config.java",
                        "password=********"
                );

        assertThat(
                generator.generate(unixPath)
        ).isEqualTo(
                generator.generate(windowsPath)
        );
    }

    @Test
    void shouldNormalizeEvidenceWhitespaceAroundValue() {
        RuleMatch first =
                match(
                        "Config.java",
                        "password=********"
                );

        RuleMatch second =
                match(
                        "Config.java",
                        "   password=********   "
                );

        assertThat(
                generator.generate(first)
        ).isEqualTo(
                generator.generate(second)
        );
    }

    @Test
    void shouldGenerateDifferentFingerprintForDifferentPath() {
        RuleMatch first =
                match(
                        "Config.java",
                        "password=********"
                );

        RuleMatch second =
                match(
                        "OtherConfig.java",
                        "password=********"
                );

        assertThat(
                generator.generate(first)
        ).isNotEqualTo(
                generator.generate(second)
        );
    }

    @Test
    void shouldGenerateDifferentFingerprintForDifferentEvidence() {
        RuleMatch first =
                match(
                        "Config.java",
                        "password=********"
                );

        RuleMatch second =
                match(
                        "Config.java",
                        "apiKey=********"
                );

        assertThat(
                generator.generate(first)
        ).isNotEqualTo(
                generator.generate(second)
        );
    }

    @Test
    void shouldRejectNullMatch() {
        assertThatThrownBy(() ->
                generator.generate(null)
        )
                .isInstanceOf(
                        NullPointerException.class
                )
                .hasMessage(
                        "match must not be null"
                );
    }

    private RuleMatch match(
            String path,
            String evidence
    ) {
        return new RuleMatch(
                "CF-SEC-001",
                Severity.CRITICAL,
                path,
                2,
                2,
                evidence
        );
    }
}