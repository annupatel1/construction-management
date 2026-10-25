package com.construction.construction_management.service;

import com.construction.construction_management.dto.ExpenseRequest;
import com.construction.construction_management.dto.ExpenseResponse;

import java.util.List;

public interface ExpenseService {
    ExpenseResponse addExpense(int projectId, ExpenseRequest request);
    List<ExpenseResponse> getExpensesByProject(int projectId);
}
