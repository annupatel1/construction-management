package com.construction.construction_management.dto;

import com.construction.construction_management.entity.PaymentMode;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class PaymentRequest {
    @Positive(message = "Amount must be greater than 0")
    double amount;
    @NotNull(message = "Payment date is required")
    LocalDate paymentDate;
    @NotNull(message = "Payment mode is required")
    PaymentMode mode;
    String remarks;
}
