package com.construction.construction_management.service;

import com.construction.construction_management.dto.LabourPaymentRequest;
import com.construction.construction_management.dto.LabourPaymentResponse;

import java.util.List;

public interface LabourPaymentService {
    LabourPaymentResponse addLabourPayment(int assignmentId, LabourPaymentRequest request);
    List<LabourPaymentResponse> getPaymentsByAssignment(int assignmentId);
}
