package com.construction.construction_management.repository;

import com.construction.construction_management.entity.AssignmentStatus;
import com.construction.construction_management.entity.LabourAssignment;
import com.construction.construction_management.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LabourAssignmentRepository extends JpaRepository<LabourAssignment, Integer> {
    List<LabourAssignment> findByProjectId(int projectId);
    List<LabourAssignment> findByLabourId(int labourId);
    List<LabourAssignment> findByProjectContractorId(int contractorId);
    List<LabourAssignment> findByProjectHouseOwnerId(int ownerId);
    Page<LabourAssignment> findByProjectId(int projectId, Pageable pageable);
    Page<LabourAssignment> findByLabourId(int labourId, Pageable pageable);
    Page<LabourAssignment> findByProjectContractorId(int contractorId, Pageable pageable);
    Page<LabourAssignment> findByProjectHouseOwnerId(int ownerId, Pageable pageable);
    boolean existsByLabourIdAndProjectId(int labourId, int projectId);
    boolean existsByLabourIdAndProjectIdAndStatus(int labourId, int projectId, AssignmentStatus status);

    /** Distinct projects for a labourer, even if they were assigned to the same site more than once. */
    @Query(value = "select distinct a.project from LabourAssignment a where a.labour.id = :labourId",
            countQuery = "select count(distinct a.project.id) from LabourAssignment a where a.labour.id = :labourId")
    Page<Project> findDistinctProjectsByLabourId(@Param("labourId") int labourId, Pageable pageable);
}
