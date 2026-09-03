package com.codefortress.analysis;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AnalysisRepository
        extends JpaRepository<Analysis, UUID> {

    Optional<Analysis> findByIdAndProject_Owner_Id(
            UUID analysisId,
            UUID ownerId
    );

    Optional<Analysis> findTopByProject_IdOrderBySequenceNumberDesc(
            UUID projectId
    );

    List<Analysis> findAllByProject_IdOrderBySequenceNumberDesc(
            UUID projectId
    );
}