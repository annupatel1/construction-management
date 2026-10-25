package com.construction.construction_management.controller;

import com.construction.construction_management.dto.ExpenseRequest;
import com.construction.construction_management.dto.ExpenseResponse;
import com.construction.construction_management.service.ExpenseService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @PreAuthorize("hasRole('CONTRACTOR')")
    @PostMapping("/{projectId}")
    public ExpenseResponse addExpense(@PathVariable int projectId,
                                      @Valid @RequestBody ExpenseRequest request) {
        return expenseService.addExpense(projectId, request);
    }

    @GetMapping("/project/{projectId}")
    public List<ExpenseResponse> getExpensesByProject(@PathVariable int projectId) {
        return expenseService.getExpensesByProject(projectId);
    }

}