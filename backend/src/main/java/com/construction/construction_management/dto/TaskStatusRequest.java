package com.construction.construction_management.dto;

import com.construction.construction_management.entity.TaskStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskStatusRequest {
    @NotNull(message = "Task status is required")
    TaskStatus status;

    @Size(max = 1000, message = "Delay reason is too long")
    String delayReason;
}
