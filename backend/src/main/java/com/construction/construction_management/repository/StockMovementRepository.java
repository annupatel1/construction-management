package com.construction.construction_management.repository;

import com.construction.construction_management.entity.StockMovement;
import com.construction.construction_management.entity.StockMovementType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Collection;

public interface StockMovementRepository extends JpaRepository<StockMovement, Integer> {

    Page<StockMovement> findByProjectIdOrderByMovementDateDescIdDesc(int projectId, Pageable pageable);

    boolean existsByMaterialId(int materialId);

    @Query("""
            select coalesce(sum(case when m.movementType in :inbound then m.quantity else -m.quantity end), 0)
            from StockMovement m
            where m.project.id = :projectId and m.material.id = :materialId
            """)
    BigDecimal balance(@Param("projectId") int projectId,
                       @Param("materialId") int materialId,
                       @Param("inbound") Collection<StockMovementType> inbound);
}
