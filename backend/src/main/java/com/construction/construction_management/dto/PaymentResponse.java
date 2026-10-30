package com.construction.construction_management.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
public class PaymentResponse {

    int id;
    int projectId;
    BigDecimal amount;
    LocalDate paymentDate;
    String mode;
    String remarks;

}