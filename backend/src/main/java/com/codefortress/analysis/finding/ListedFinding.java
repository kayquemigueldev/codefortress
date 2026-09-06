package com.codefortress.analysis.finding;

import com.codefortress.analysis.Finding;
import com.codefortress.analysis.FindingCategory;
import com.codefortress.analysis.FindingStatus;
import com.codefortress.analysis.Severity;

import java.time.Instant;
import java.util.UUID;

public record ListedFinding(
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

    public static ListedFinding from(
            Finding finding
    ) {
        return new ListedFinding(
                finding.getId(),
                finding.getRuleKey(),
                finding.getRuleVersion(),
                finding.getTitle(),
                finding.getCategory(),
                finding.getSeverity(),
                finding.getStatus(),
                finding.getFilePath(),
                finding.getStartLine(),
                finding.getEndLine(),
                finding.getCodeExcerpt(),
                finding.getDescription(),
                finding.getImpact(),
                finding.getRecommendation(),
                finding.getFingerprint(),
                finding.getCreatedAt(),
                finding.getStatusUpdatedAt()
        );
    }
}