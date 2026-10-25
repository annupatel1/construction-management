package com.construction.construction_management.repository;

import com.construction.construction_management.entity.ProjectRequirement;
import com.construction.construction_management.entity.RequirementStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectRequirementRepository extends JpaRepository<ProjectRequirement, Integer> {
    Page<ProjectRequirement> findByHouseOwnerId(int ownerId, Pageable pageable);
    Page<ProjectRequirement> findByStatus(RequirementStatus status, Pageable pageable);
    Page<ProjectRequirement> findByCityIgnoreCaseAndStatus(String city, RequirementStatus status, Pageable pageable);
}
