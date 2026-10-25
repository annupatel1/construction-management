package com.construction.construction_management.service;

import com.construction.construction_management.dto.PaymentRequest;
import com.construction.construction_management.dto.PaymentResponse;

import java.util.List;

public interface PaymentService {
    PaymentResponse addPayment(int projectId, PaymentRequest request);
    List<PaymentResponse> getPaymentsByProject(int projectId);
}
