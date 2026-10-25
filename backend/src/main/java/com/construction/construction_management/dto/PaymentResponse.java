package com.construction.construction_management.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
public class PaymentResponse {

    int id;
    int projectId;
    double amount;
    LocalDate paymentDate;
    String mode;
    String remarks;

}