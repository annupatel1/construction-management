package com.construction.construction_management.repository;

import com.construction.construction_management.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Integer> {
    List<Project> findByHouseOwnerId(int ownerId);
    List<Project> findByContractorId(int contractorId);
}
