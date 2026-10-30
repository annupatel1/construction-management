package com.construction.construction_management.repository;

import com.construction.construction_management.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Integer> {
    Page<Project> findByHouseOwnerId(int ownerId, Pageable pageable);
    Page<Project> findByContractorId(int contractorId, Pageable pageable);
}
