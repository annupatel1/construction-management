package com.construction.construction_management.controller;

import com.construction.construction_management.dto.PaymentRequest;
import com.construction.construction_management.dto.PaymentResponse;
import com.construction.construction_management.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PreAuthorize("hasRole('HOUSE_OWNER')")
    @PostMapping("/{projectId}")
    public PaymentResponse addPayment(@PathVariable int projectId,
                                      @Valid @RequestBody PaymentRequest request) {
        return paymentService.addPayment(projectId, request);
    }

    @GetMapping("/project/{projectId}")
    public List<PaymentResponse> getPaymentsByProject(@PathVariable int projectId) {
        return paymentService.getPaymentsByProject(projectId);
    }

}