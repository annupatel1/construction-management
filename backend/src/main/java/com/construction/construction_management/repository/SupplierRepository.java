package com.construction.construction_management.repository;

import com.construction.construction_management.entity.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SupplierRepository extends JpaRepository<Supplier, Integer> {

    Page<Supplier> findByProjectIdAndActiveTrueOrderByNameAscIdAsc(int projectId, Pageable pageable);

    Page<Supplier> findByProjectIdOrderByNameAscIdAsc(int projectId, Pageable pageable);

    Optional<Supplier> findByIdAndProjectId(int id, int projectId);
}
