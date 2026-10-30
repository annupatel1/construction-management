package com.construction.construction_management.repository;

import com.construction.construction_management.entity.ProjectIssue;
import com.construction.construction_management.entity.IssueStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectIssueRepository extends JpaRepository<ProjectIssue, Integer> {

    @EntityGraph(attributePaths = {"raisedBy", "assignee"})
    Page<ProjectIssue> findByProjectIdOrderByCreatedAtDescIdDesc(int projectId, Pageable pageable);

    @EntityGraph(attributePaths = {"raisedBy", "assignee"})
    List<ProjectIssue> findByProjectIdOrderByCreatedAtDescIdDesc(int projectId);

    @EntityGraph(attributePaths = {"raisedBy", "assignee"})
    Optional<ProjectIssue> findByIdAndProjectId(int id, int projectId);

    long countByProjectIdAndStatusNot(int projectId, IssueStatus status);
}
