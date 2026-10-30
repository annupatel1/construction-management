package com.construction.construction_management.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class MaterialRequestRequest {

    @NotNull(message = "Material is required")
    Integer materialId;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be greater than 0")
    BigDecimal quantity;

    LocalDate neededBy;

    @Size(max = 2000, message = "Notes are too long")
    String notes;
}
