package com.construction.construction_management.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
public class ExpenseResponse {

    int id;
    int projectId;
    String category;
    double amount;
    double quantity;
    String unit;
    LocalDate purchaseDate;

}