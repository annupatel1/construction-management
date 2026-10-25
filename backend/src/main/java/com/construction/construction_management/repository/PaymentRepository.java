package com.construction.construction_management.repository;

import com.construction.construction_management.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Integer> {

    List<Payment> findByProjectId(int projectId);

}