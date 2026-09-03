package com.codefortress.analysis;

import com.codefortress.project.Project;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "analyses")
public class Analysis {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(name = "sequence_number", nullable = false)
    private int sequenceNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AnalysisStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 20)
    private AnalysisSourceType sourceType;

    @Column(name = "source_reference", nullable = false, length = 255)
    private String sourceReference;

    @Column(name = "source_filename", nullable = false, length = 255)
    private String sourceFilename;

    @Column(name = "rule_set_version", nullable = false, length = 30)
    private String ruleSetVersion;

    @Column(name = "score_version", nullable = false, length = 30)
    private String scoreVersion;

    @Column(name = "security_score")
    private Short securityScore;

    @Column(name = "files_scanned")
    private Integer filesScanned;

    @Column(name = "lines_scanned")
    private Long linesScanned;

    @Column(name = "findings_count")
    private Integer findingsCount;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "failure_code", length = 50)
    private String failureCode;

    @Column(name = "failure_message", length = 500)
    private String failureMessage;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected Analysis() {
    }

    private Analysis(
            Project project,
            int sequenceNumber,
            String sourceReference,
            String sourceFilename,
            String ruleSetVersion,
            String scoreVersion
    ) {
        if (sequenceNumber < 1) {
            throw new IllegalArgumentException(
                    "sequenceNumber must be greater than zero"
            );
        }

        this.project = Objects.requireNonNull(
                project,
                "project must not be null"
        );

        this.sequenceNumber = sequenceNumber;
        this.status = AnalysisStatus.QUEUED;
        this.sourceType = AnalysisSourceType.UPLOAD;
        this.sourceReference = requireText(
                sourceReference,
                "sourceReference"
        );
        this.sourceFilename = requireText(
                sourceFilename,
                "sourceFilename"
        );
        this.ruleSetVersion = requireText(
                ruleSetVersion,
                "ruleSetVersion"
        );
        this.scoreVersion = requireText(
                scoreVersion,
                "scoreVersion"
        );
    }

    public static Analysis queueUpload(
            Project project,
            int sequenceNumber,
            String sourceReference,
            String sourceFilename,
            String ruleSetVersion,
            String scoreVersion
    ) {
        return new Analysis(
                project,
                sequenceNumber,
                sourceReference,
                sourceFilename,
                ruleSetVersion,
                scoreVersion
        );
    }

    @PrePersist
    private void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }

        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    private static String requireText(
            String value,
            String fieldName
    ) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    fieldName + " must not be blank"
            );
        }

        return value.trim();
    }

    public UUID getId() {
        return id;
    }

    public Project getProject() {
        return project;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public AnalysisStatus getStatus() {
        return status;
    }

    public AnalysisSourceType getSourceType() {
        return sourceType;
    }

    public String getSourceReference() {
        return sourceReference;
    }

    public String getSourceFilename() {
        return sourceFilename;
    }

    public String getRuleSetVersion() {
        return ruleSetVersion;
    }

    public String getScoreVersion() {
        return scoreVersion;
    }

    public Short getSecurityScore() {
        return securityScore;
    }

    public Integer getFilesScanned() {
        return filesScanned;
    }

    public Long getLinesScanned() {
        return linesScanned;
    }

    public Integer getFindingsCount() {
        return findingsCount;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public String getFailureCode() {
        return failureCode;
    }

    public String getFailureMessage() {
        return failureMessage;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}