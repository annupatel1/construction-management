package com.construction.construction_management.repository;

import com.construction.construction_management.entity.MaterialItem;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MaterialItemRepository extends JpaRepository<MaterialItem, Integer> {

    Page<MaterialItem> findByProjectIdAndActiveTrueOrderByNameAscIdAsc(int projectId, Pageable pageable);

    Page<MaterialItem> findByProjectIdOrderByNameAscIdAsc(int projectId, Pageable pageable);

    Optional<MaterialItem> findByIdAndProjectId(int id, int projectId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from MaterialItem m where m.id = :id and m.project.id = :projectId")
    Optional<MaterialItem> lockByIdAndProjectId(@Param("id") int id, @Param("projectId") int projectId);

    boolean existsByProjectIdAndNameIgnoreCaseAndUnitIgnoreCase(int projectId, String name, String unit);

    boolean existsByProjectIdAndNameIgnoreCaseAndUnitIgnoreCaseAndIdNot(
            int projectId, String name, String unit, int id);
}
