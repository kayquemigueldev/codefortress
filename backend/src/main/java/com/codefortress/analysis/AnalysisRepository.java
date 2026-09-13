package com.codefortress.analysis;

import com.codefortress.project.ProjectStatus;
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

    Optional<Analysis> findByIdAndProject_IdAndProject_Owner_Id(
            UUID analysisId,
            UUID projectId,
            UUID ownerId
    );

    Optional<Analysis> findTopByProject_IdOrderBySequenceNumberDesc(
            UUID projectId
    );

    List<Analysis> findAllByProject_IdOrderBySequenceNumberDesc(
            UUID projectId
    );

    Optional<Analysis> findTopByProject_Owner_IdAndProject_StatusOrderByCreatedAtDesc(
            UUID ownerId,
            ProjectStatus projectStatus
    );

    List<Analysis> findTop5ByProject_Owner_IdAndProject_StatusOrderByCreatedAtDesc(
            UUID ownerId,
            ProjectStatus projectStatus
    );

    @Query("""
        SELECT AVG(analysis.securityScore)
        FROM Analysis analysis
        WHERE analysis.project.owner.id = :ownerId
          AND analysis.project.status = :projectStatus
          AND analysis.status = :analysisStatus
          AND analysis.securityScore IS NOT NULL
          AND analysis.sequenceNumber = (
              SELECT MAX(latest.sequenceNumber)
              FROM Analysis latest
              WHERE latest.project.id = analysis.project.id
                AND latest.status = :analysisStatus
                AND latest.securityScore IS NOT NULL
          )
        """)
    Double findAverageLatestSecurityScore(
            @Param("ownerId") UUID ownerId,
            @Param("projectStatus") ProjectStatus projectStatus,
            @Param("analysisStatus") AnalysisStatus analysisStatus
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