package com.construction.construction_management.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LabourAssignmentUpdateRequest {
    @Positive(message = "Daily rate must be greater than 0")
    double dailyRate;
    @PositiveOrZero(message = "Advance cannot be negative")
    double advanceTaken;
}
