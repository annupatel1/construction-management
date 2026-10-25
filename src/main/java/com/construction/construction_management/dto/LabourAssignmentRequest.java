package com.construction.construction_management.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class LabourAssignmentRequest {
    @Positive(message = "Daily rate must be greater than 0")
    double dailyRate;
    @PositiveOrZero(message = "Advance taken cannot be negative")
    double advanceTaken;
    /** Optional - defaults to today. */
    LocalDate assignedDate;
}
