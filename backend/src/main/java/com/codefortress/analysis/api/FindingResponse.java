package com.codefortress.analysis.api;

import com.codefortress.analysis.FindingCategory;
import com.codefortress.analysis.FindingStatus;
import com.codefortress.analysis.Severity;
import com.codefortress.analysis.finding.ListedFinding;

import java.time.Instant;
import java.util.UUID;

public record FindingResponse(
        UUID id,
        String ruleKey,
        String ruleVersion,
        String title,
        FindingCategory category,
        Severity severity,
        FindingStatus status,
        String filePath,
        int startLine,
        int endLine,
        String codeExcerpt,
        String description,
        String impact,
        String recommendation,
        String fingerprint,
        Instant createdAt,
        Instant statusUpdatedAt
) {

    public static FindingResponse from(
            ListedFinding finding
    ) {
        return new FindingResponse(
                finding.id(),
                finding.ruleKey(),
                finding.ruleVersion(),
                finding.title(),
                finding.category(),
                finding.severity(),
                finding.status(),
                finding.filePath(),
                finding.startLine(),
                finding.endLine(),
                finding.codeExcerpt(),
                finding.description(),
                finding.impact(),
                finding.recommendation(),
                finding.fingerprint(),
                finding.createdAt(),
                finding.statusUpdatedAt()
        );
    }
}