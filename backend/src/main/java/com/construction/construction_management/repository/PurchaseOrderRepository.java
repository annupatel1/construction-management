package com.construction.construction_management.repository;

import com.construction.construction_management.entity.PurchaseOrder;
import com.construction.construction_management.entity.PurchaseOrderStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Integer> {

    Page<PurchaseOrder> findByProjectIdOrderByCreatedAtDescIdDesc(int projectId, Pageable pageable);

    Optional<PurchaseOrder> findByIdAndProjectId(int id, int projectId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from PurchaseOrder p where p.id = :id and p.project.id = :projectId")
    Optional<PurchaseOrder> lockByIdAndProjectId(@Param("id") int id, @Param("projectId") int projectId);

    boolean existsByMaterialRequestIdAndStatusNot(int materialRequestId, PurchaseOrderStatus status);
}
