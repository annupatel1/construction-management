package com.construction.construction_management.service.impl;

import com.construction.construction_management.dto.ExpenseRequest;
import com.construction.construction_management.dto.ExpenseResponse;
import com.construction.construction_management.entity.Expense;
import com.construction.construction_management.entity.Project;
import com.construction.construction_management.repository.ExpenseRepository;
import com.construction.construction_management.service.AccessGuard;
import com.construction.construction_management.service.ExpenseService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final AccessGuard guard;

    public ExpenseServiceImpl(ExpenseRepository expenseRepository, AccessGuard guard) {
        this.expenseRepository = expenseRepository;
        this.guard = guard;
    }

    private ExpenseResponse toResponse(Expense e) {
        return new ExpenseResponse(
                e.getId(),
                e.getProject().getId(),
                e.getCategory().toString(),
                e.getAmount(),
                e.getQuantity(),
                e.getUnit(),
                e.getPurchaseDate()
        );
    }

    @Override
    public ExpenseResponse addExpense(int projectId, ExpenseRequest request) {
        Project project = guard.projectForContractor(projectId);

        Expense e = new Expense();
        e.setProject(project);
        e.setCategory(request.getCategory());
        e.setAmount(request.getAmount());
        e.setQuantity(request.getQuantity());
        e.setUnit(request.getUnit().trim());
        e.setPurchaseDate(request.getPurchaseDate());
        return toResponse(expenseRepository.save(e));
    }

    @Override
    public List<ExpenseResponse> getExpensesByProject(int projectId) {
        guard.projectForOwnerOrContractor(projectId);
        return expenseRepository.findByProjectId(projectId).stream().map(this::toResponse).toList();
    }
}
