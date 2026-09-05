package com.codefortress.analysis;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FindingRepository
        extends JpaRepository<Finding, UUID> {

    List<Finding> findAllByAnalysis_IdOrderByCreatedAtAsc(
            UUID analysisId
    );

    boolean existsByAnalysis_IdAndFingerprint(
            UUID analysisId,
            String fingerprint
    );
}