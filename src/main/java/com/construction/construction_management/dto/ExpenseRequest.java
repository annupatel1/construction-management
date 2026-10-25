package com.construction.construction_management.dto;

import com.construction.construction_management.entity.MaterialCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class ExpenseRequest {
    @NotNull(message = "Category is required")
    MaterialCategory category;
    @Positive(message = "Amount must be greater than 0")
    double amount;
    @Positive(message = "Quantity must be greater than 0")
    double quantity;
    @NotBlank(message = "Unit is required")
    String unit;
    @NotNull(message = "Purchase date is required")
    LocalDate purchaseDate;
}
