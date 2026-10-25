package com.construction.construction_management.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class LabourAssignmentRequest {
    @Positive(message = "Daily rate must be greater than 0")
    @NotNull(message = "dailyRate is required")
    BigDecimal dailyRate;
    @PositiveOrZero(message = "Advance taken cannot be negative")
    @NotNull(message = "advanceTaken is required")
    BigDecimal advanceTaken;
    /** Optional - defaults to today. */
    LocalDate assignedDate;
}
