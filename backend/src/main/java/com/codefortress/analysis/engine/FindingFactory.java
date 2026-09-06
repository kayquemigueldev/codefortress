package com.codefortress.analysis.engine;

import com.codefortress.analysis.Analysis;
import com.codefortress.analysis.Finding;

import java.util.Objects;

public class FindingFactory {

    private final FindingFingerprintGenerator fingerprintGenerator;

    public FindingFactory(
            FindingFingerprintGenerator fingerprintGenerator
    ) {
        this.fingerprintGenerator =
                Objects.requireNonNull(
                        fingerprintGenerator,
                        "fingerprintGenerator must not be null"
                );
    }

    public Finding create(
            Analysis analysis,
            EvaluatedRuleMatch evaluatedMatch
    ) {
        Objects.requireNonNull(
                analysis,
                "analysis must not be null"
        );

        Objects.requireNonNull(
                evaluatedMatch,
                "evaluatedMatch must not be null"
        );

        RuleMetadata metadata =
                evaluatedMatch.metadata();

        RuleMatch match =
                evaluatedMatch.match();

        String fingerprint =
                fingerprintGenerator.generate(
                        match
                );

        return Finding.create(
                analysis,
                metadata.key(),
                metadata.version(),
                fingerprint,
                metadata.title(),
                metadata.category(),
                match.severity(),
                match.filePath(),
                match.startLine(),
                match.endLine(),
                match.redactedEvidence(),
                metadata.description(),
                metadata.impact(),
                metadata.recommendation()
        );
    }
}