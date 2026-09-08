package com.codefortress.analysis;

import org.springframework.data.jpa.repository.JpaRepository;
import com.codefortress.project.ProjectStatus;

import java.util.List;
import java.util.UUID;
import java.util.Optional;

public interface FindingRepository
        extends JpaRepository<Finding, UUID> {

    List<Finding> findAllByAnalysis_IdOrderByCreatedAtAsc(
            UUID analysisId
    );

    Optional<Finding> findByIdAndAnalysis_Id(
            UUID findingId,
            UUID analysisId
    );

    long countByAnalysis_Project_Owner_IdAndAnalysis_Project_StatusAndStatus(
            UUID ownerId,
            ProjectStatus projectStatus,
            FindingStatus status
    );

    long countByAnalysis_Project_Owner_IdAndAnalysis_Project_StatusAndStatusAndSeverity(
            UUID ownerId,
            ProjectStatus projectStatus,
            FindingStatus status,
            Severity severity
    );

    boolean existsByAnalysis_IdAndFingerprint(
            UUID analysisId,
            String fingerprint
    );
}