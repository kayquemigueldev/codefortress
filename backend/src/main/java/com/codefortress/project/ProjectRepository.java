package com.codefortress.project;

import org.springframework.data.jpa.repository.JpaRepository;

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
            UUID excludedProjectId
    );

    Optional<Project> findByIdAndOwner_Id(
            UUID projectId,
            UUID ownerId
    );

    List<Project> findAllByOwner_IdAndStatusOrderByCreatedAtDesc(
            UUID ownerId,
            ProjectStatus status
    );
}