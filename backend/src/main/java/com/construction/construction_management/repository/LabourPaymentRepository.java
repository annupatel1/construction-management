package com.construction.construction_management.repository;

import com.construction.construction_management.entity.LabourPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LabourPaymentRepository extends JpaRepository<LabourPayment, Integer> {

    List<LabourPayment> findByLabourAssignmentId(int labourAssignmentId);

    @Query("""
            select p from LabourPayment p
            join fetch p.labourAssignment a
            join fetch a.labour
            where a.project.id = :projectId
            order by p.paymentDate asc, p.id asc
            """)
    List<LabourPayment> findAllForProjectExport(@Param("projectId") int projectId);
}
