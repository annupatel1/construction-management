package com.construction.construction_management.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class TaskRequest {
    @NotBlank(message = "Task title is required")
    @Size(max = 255, message = "Task title is too long")
    String title;

    @Size(max = 2000, message = "Task description is too long")
    String description;

    Integer milestoneId;

    Integer assigneeId;

    LocalDate dueDate;

    @Size(max = 1000, message = "Delay reason is too long")
    String delayReason;
}
