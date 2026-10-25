package com.construction.construction_management.repository;

import com.construction.construction_management.entity.Milestone;
import com.construction.construction_management.entity.MilestoneStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MilestoneRepository extends JpaRepository<Milestone, Integer> {

    Page<Milestone> findByProjectIdOrderByTargetDateAscIdAsc(int projectId, Pageable pageable);

    Optional<Milestone> findByIdAndProjectId(int id, int projectId);

    long countByProjectId(int projectId);

    long countByProjectIdAndStatus(int projectId, MilestoneStatus status);
}
