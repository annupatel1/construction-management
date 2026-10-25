package com.construction.construction_management.repository;

import com.construction.construction_management.entity.AssignmentStatus;
import com.construction.construction_management.entity.LabourAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LabourAssignmentRepository extends JpaRepository<LabourAssignment, Integer> {
    List<LabourAssignment> findByProjectId(int projectId);
    List<LabourAssignment> findByLabourId(int labourId);
    List<LabourAssignment> findByProjectContractorId(int contractorId);
    List<LabourAssignment> findByProjectHouseOwnerId(int ownerId);
    boolean existsByLabourIdAndProjectId(int labourId, int projectId);
    boolean existsByLabourIdAndProjectIdAndStatus(int labourId, int projectId, AssignmentStatus status);
}
