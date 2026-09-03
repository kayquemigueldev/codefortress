package com.codefortress.project;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectRepository
        extends JpaRepository<Project, UUID> {

    boolean existsByOwner_IdAndNameIgnoreCaseAndStatus(
            UUID ownerId,
            String name,
            ProjectStatus status
    );

    boolean existsByOwner_IdAndNameIgnoreCaseAndStatusAndIdNot(
            UUID ownerId,
            String name,
            ProjectStatus status,
            UUID projectId
    );

    Optional<Project> findByIdAndOwner_Id(
            UUID projectId,
            UUID ownerId
    );

    List<Project> findAllByOwner_IdAndStatusOrderByCreatedAtDesc(
            UUID ownerId,
            ProjectStatus status
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT project
            FROM Project project
            WHERE project.id = :projectId
              AND project.owner.id = :ownerId
            """)
    Optional<Project> findOwnedByIdForUpdate(
            @Param("projectId") UUID projectId,
            @Param("ownerId") UUID ownerId
    );
}