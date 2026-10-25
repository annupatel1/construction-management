package com.construction.construction_management.repository;

import com.construction.construction_management.entity.LabourPayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LabourPaymentRepository extends JpaRepository<LabourPayment, Integer> {

    List<LabourPayment> findByLabourAssignmentId(int labourAssignmentId);

}