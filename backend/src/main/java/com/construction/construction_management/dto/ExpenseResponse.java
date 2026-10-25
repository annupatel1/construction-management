package com.construction.construction_management.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
public class ExpenseResponse {

    int id;
    int projectId;
    String category;
    BigDecimal amount;
    BigDecimal quantity;
    String unit;
    LocalDate purchaseDate;

}