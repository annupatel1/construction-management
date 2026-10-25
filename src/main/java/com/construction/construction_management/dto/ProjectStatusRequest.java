package com.construction.construction_management.dto;

import com.construction.construction_management.entity.ProjectStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProjectStatusRequest {
    @NotNull(message = "Status is required")
    ProjectStatus status;
}
