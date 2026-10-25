package com.construction.construction_management.repository;

import com.construction.construction_management.entity.MaterialRequest;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MaterialRequestRepository extends JpaRepository<MaterialRequest, Integer> {

    Page<MaterialRequest> findByProjectIdOrderByCreatedAtDescIdDesc(int projectId, Pageable pageable);

    Optional<MaterialRequest> findByIdAndProjectId(int id, int projectId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from MaterialRequest r where r.id = :id and r.project.id = :projectId")
    Optional<MaterialRequest> lockByIdAndProjectId(@Param("id") int id, @Param("projectId") int projectId);
}
