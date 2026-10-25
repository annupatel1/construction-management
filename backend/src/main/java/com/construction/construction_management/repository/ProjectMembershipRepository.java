package com.construction.construction_management.repository;

import com.construction.construction_management.entity.Project;
import com.construction.construction_management.entity.ProjectMembership;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProjectMembershipRepository extends JpaRepository<ProjectMembership, Integer> {

    boolean existsByProjectIdAndUserIdAndActiveTrue(int projectId, int userId);

    Optional<ProjectMembership> findByProjectIdAndUserId(int projectId, int userId);

    @EntityGraph(attributePaths = "user")
    List<ProjectMembership> findByProjectIdAndActiveTrueOrderByIdAsc(int projectId);

    @EntityGraph(attributePaths = {"user", "project"})
    Optional<ProjectMembership> findByIdAndProjectId(int id, int projectId);

    @Query(value = "select distinct m.project from ProjectMembership m "
            + "where m.user.id = :userId and m.active = true",
            countQuery = "select count(distinct m.project.id) from ProjectMembership m "
                    + "where m.user.id = :userId and m.active = true")
    Page<Project> findDistinctProjectsByUserId(@Param("userId") int userId, Pageable pageable);
}
