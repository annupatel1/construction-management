package com.construction.construction_management.repository;

import com.construction.construction_management.entity.ProgressReport;
import com.construction.construction_management.entity.ProgressReportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ProgressReportRepository extends JpaRepository<ProgressReport, Integer> {

    Page<ProgressReport> findByProjectIdOrderByReportDateDescIdDesc(int projectId, Pageable pageable);

    @EntityGraph(attributePaths = {"author"})
    List<ProgressReport> findByProjectIdOrderByReportDateDescIdDesc(int projectId);

    Page<ProgressReport> findByProjectIdAndStatusOrderByReportDateDescIdDesc(
            int projectId, ProgressReportStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"author", "photos"})
    Optional<ProgressReport> findByIdAndProjectId(int id, int projectId);

    boolean existsByProjectIdAndReportDate(int projectId, LocalDate reportDate);
}
