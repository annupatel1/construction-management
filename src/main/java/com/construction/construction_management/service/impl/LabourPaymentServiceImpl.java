package com.construction.construction_management.service.impl;

import com.construction.construction_management.dto.LabourPaymentRequest;
import com.construction.construction_management.dto.LabourPaymentResponse;
import com.construction.construction_management.entity.LabourAssignment;
import com.construction.construction_management.entity.LabourPayment;
import com.construction.construction_management.repository.LabourPaymentRepository;
import com.construction.construction_management.service.AccessGuard;
import com.construction.construction_management.service.LabourPaymentService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LabourPaymentServiceImpl implements LabourPaymentService {

    private final LabourPaymentRepository labourPaymentRepository;
    private final AccessGuard guard;

    public LabourPaymentServiceImpl(LabourPaymentRepository labourPaymentRepository, AccessGuard guard) {
        this.labourPaymentRepository = labourPaymentRepository;
        this.guard = guard;
    }

    private LabourPaymentResponse toResponse(LabourPayment p) {
        return new LabourPaymentResponse(
                p.getId(),
                p.getLabourAssignment().getId(),
                p.getAmount(),
                p.getPaymentDate(),
                p.getMode().toString(),
                p.getRemarks()
        );
    }

    @Override
    public LabourPaymentResponse addLabourPayment(int assignmentId, LabourPaymentRequest request) {
        LabourAssignment assignment = guard.assignmentForContractor(assignmentId);

        LabourPayment p = new LabourPayment();
        p.setLabourAssignment(assignment);
        p.setAmount(request.getAmount());
        p.setPaymentDate(request.getPaymentDate());
        p.setMode(request.getMode());
        p.setRemarks(request.getRemarks());
        return toResponse(labourPaymentRepository.save(p));
    }

    @Override
    public List<LabourPaymentResponse> getPaymentsByAssignment(int assignmentId) {
        guard.assignmentForWages(assignmentId);
        return labourPaymentRepository.findByLabourAssignmentId(assignmentId).stream()
                .map(this::toResponse)
                .toList();
    }
}
