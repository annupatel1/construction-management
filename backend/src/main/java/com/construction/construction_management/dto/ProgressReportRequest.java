package com.construction.construction_management.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class ProgressReportRequest {
    @NotNull(message = "Report date is required")
    LocalDate reportDate;

    @NotBlank(message = "Planned work is required")
    @Size(max = 2000, message = "Planned work is too long")
    String plannedWork;

    @NotBlank(message = "Completed work is required")
    @Size(max = 2000, message = "Completed work is too long")
    String completedWork;

    @Size(max = 2000, message = "Blockers are too long")
    String blockers;

    @Size(max = 2000, message = "Next-day plan is too long")
    String nextDayPlan;

    @Valid
    @Size(max = 10, message = "Add at most 10 photos")
    List<ProgressPhotoRequest> photos;
}
