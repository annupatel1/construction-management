package com.construction.construction_management.dto;

import com.construction.construction_management.entity.MilestoneStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class MilestoneRequest {
    @NotBlank(message = "Milestone title is required")
    @Size(max = 255, message = "Milestone title is too long")
    String title;

    @Size(max = 1000, message = "Milestone description is too long")
    String description;

    @NotNull(message = "Target date is required")
    LocalDate targetDate;

    MilestoneStatus status;

    LocalDate completedDate;
}
