package com.construction.construction_management.repository;

import com.construction.construction_management.entity.ProjectTask;
import com.construction.construction_management.entity.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface ProjectTaskRepository extends JpaRepository<ProjectTask, Integer>, JpaSpecificationExecutor<ProjectTask> {

    List<ProjectTask> findByProjectIdOrderByDueDateAscIdDesc(int projectId);

    Optional<ProjectTask> findByIdAndProjectId(int id, int projectId);

    List<ProjectTask> findByMilestoneId(int milestoneId);

    long countByProjectId(int projectId);

    long countByProjectIdAndStatus(int projectId, TaskStatus status);

    long countByMilestoneId(int milestoneId);

    long countByMilestoneIdAndStatus(int milestoneId, TaskStatus status);
}
