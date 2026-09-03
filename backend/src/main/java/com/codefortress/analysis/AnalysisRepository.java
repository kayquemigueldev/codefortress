package com.codefortress.analysis;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT analysis
            FROM Analysis analysis
            WHERE analysis.id = :analysisId
            """)
    Optional<Analysis> findByIdForUpdate(
            @Param("analysisId") UUID analysisId
    );
}