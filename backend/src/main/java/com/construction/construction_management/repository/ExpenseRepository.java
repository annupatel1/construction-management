package com.construction.construction_management.repository;

import com.construction.construction_management.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Integer> {

    List<Expense> findByProjectId(int projectId);

}