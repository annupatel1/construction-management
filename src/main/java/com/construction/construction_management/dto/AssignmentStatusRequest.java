package com.construction.construction_management.dto;

import com.construction.construction_management.entity.AssignmentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AssignmentStatusRequest {
    @NotNull(message = "Status is required")
    AssignmentStatus status;
}
