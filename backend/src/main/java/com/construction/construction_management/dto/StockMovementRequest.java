package com.construction.construction_management.dto;

import com.construction.construction_management.entity.StockMovementType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class StockMovementRequest {

    @NotNull(message = "Material is required")
    Integer materialId;

    @NotNull(message = "Movement type is required")
    StockMovementType movementType;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be greater than 0")
    BigDecimal quantity;

    LocalDate movementDate;

    @Size(max = 1000, message = "Notes are too long")
    String notes;
}
