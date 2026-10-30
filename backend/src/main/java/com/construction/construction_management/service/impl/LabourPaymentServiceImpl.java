package com.construction.construction_management.service.impl;

import com.construction.construction_management.dto.LabourPaymentRequest;
import com.construction.construction_management.dto.LabourPaymentResponse;
import com.construction.construction_management.entity.LabourAssignment;
import com.construction.construction_management.entity.LabourPayment;
import com.construction.construction_management.entity.LabourPaymentType;
import com.construction.construction_management.repository.LabourPaymentRepository;
import com.construction.construction_management.service.AccessGuard;
import com.construction.construction_management.service.AuditService;
import com.construction.construction_management.service.LabourPaymentService;
import org.springframework.stereotype.Service;
import com.construction.construction_management.util.Money;

import java.util.List;

@Service
public class LabourPaymentServiceImpl implements LabourPaymentService {

    private final LabourPaymentRepository labourPaymentRepository;
    private final AccessGuard guard;
    private final AuditService auditService;

    public LabourPaymentServiceImpl(LabourPaymentRepository labourPaymentRepository, AccessGuard guard,
                                    AuditService auditService) {
        this.labourPaymentRepository = labourPaymentRepository;
        this.guard = guard;
        this.auditService = auditService;
    }

    private LabourPaymentResponse toResponse(LabourPayment p) {
        return new LabourPaymentResponse(
                p.getId(),
                p.getLabourAssignment().getId(),
                p.getAmount(),
                p.getPaymentDate(),
                p.getMode().toString(),
                p.getType() == null ? LabourPaymentType.WAGE.toString() : p.getType().toString(),
                p.getRemarks()
        );
    }

    @Override
    public LabourPaymentResponse addLabourPayment(int assignmentId, LabourPaymentRequest request) {
        LabourAssignment assignment = guard.assignmentForContractor(assignmentId);

        LabourPayment p = new LabourPayment();
        p.setLabourAssignment(assignment);
        p.setAmount(Money.money(request.getAmount()));
        p.setPaymentDate(request.getPaymentDate());
        p.setMode(request.getMode());
        p.setType(request.getType() == null ? LabourPaymentType.WAGE : request.getType());
        p.setRemarks(request.getRemarks());
        LabourPayment saved = labourPaymentRepository.save(p);
        auditService.record("LABOUR_PAYMENT_RECORDED", "LabourPayment", saved.getId(),
                "assignmentId=" + assignmentId + "; amount=" + saved.getAmount()
                        + "; type=" + saved.getType() + "; mode=" + saved.getMode());
        return toResponse(saved);
    }

    @Override
    public List<LabourPaymentResponse> getPaymentsByAssignment(int assignmentId) {
        guard.assignmentForWages(assignmentId);
        return labourPaymentRepository.findByLabourAssignmentId(assignmentId).stream()
                .map(this::toResponse)
                .toList();
    }
}
