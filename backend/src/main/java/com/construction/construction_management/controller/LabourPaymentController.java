package com.construction.construction_management.controller;

import com.construction.construction_management.dto.LabourPaymentRequest;
import com.construction.construction_management.dto.LabourPaymentResponse;
import com.construction.construction_management.service.LabourPaymentService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/labour-payments")
public class LabourPaymentController {

    private final LabourPaymentService labourPaymentService;

    public LabourPaymentController(LabourPaymentService labourPaymentService) {
        this.labourPaymentService = labourPaymentService;
    }

    @PreAuthorize("hasRole('CONTRACTOR')")
    @PostMapping("/{assignmentId}")
    public LabourPaymentResponse addLabourPayment(@PathVariable int assignmentId,
                                                  @Valid @RequestBody LabourPaymentRequest request) {
        return labourPaymentService.addLabourPayment(assignmentId, request);
    }

    @GetMapping("/assignment/{assignmentId}")
    public List<LabourPaymentResponse> getPaymentsByAssignment(@PathVariable int assignmentId) {
        return labourPaymentService.getPaymentsByAssignment(assignmentId);
    }

}