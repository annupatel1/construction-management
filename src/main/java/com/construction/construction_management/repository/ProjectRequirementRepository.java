package com.construction.construction_management.repository;

import com.construction.construction_management.entity.ProjectRequirement;
import com.construction.construction_management.entity.RequirementStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectRequirementRepository extends JpaRepository<ProjectRequirement, Integer> {
    List<ProjectRequirement> findByHouseOwnerId(int ownerId);
    List<ProjectRequirement> findByStatus(RequirementStatus status);
    List<ProjectRequirement> findByCityIgnoreCaseAndStatus(String city, RequirementStatus status);
}
