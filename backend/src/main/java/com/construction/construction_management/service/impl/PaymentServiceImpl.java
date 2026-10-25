package com.construction.construction_management.service.impl;

import com.construction.construction_management.dto.PaymentRequest;
import com.construction.construction_management.dto.PaymentResponse;
import com.construction.construction_management.entity.Payment;
import com.construction.construction_management.entity.Project;
import com.construction.construction_management.repository.PaymentRepository;
import com.construction.construction_management.service.AccessGuard;
import com.construction.construction_management.service.PaymentService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final AccessGuard guard;

    public PaymentServiceImpl(PaymentRepository paymentRepository, AccessGuard guard) {
        this.paymentRepository = paymentRepository;
        this.guard = guard;
    }

    private PaymentResponse toResponse(Payment p) {
        return new PaymentResponse(
                p.getId(),
                p.getProject().getId(),
                p.getAmount(),
                p.getPaymentDate(),
                p.getMode().toString(),
                p.getRemarks()
        );
    }

    @Override
    public PaymentResponse addPayment(int projectId, PaymentRequest request) {
        Project project = guard.projectForOwner(projectId);

        Payment p = new Payment();
        p.setProject(project);
        p.setAmount(request.getAmount());
        p.setPaymentDate(request.getPaymentDate());
        p.setMode(request.getMode());
        p.setRemarks(request.getRemarks());
        return toResponse(paymentRepository.save(p));
    }

    @Override
    public List<PaymentResponse> getPaymentsByProject(int projectId) {
        guard.projectForOwnerOrContractor(projectId);
        return paymentRepository.findByProjectId(projectId).stream().map(this::toResponse).toList();
    }
}
