package com.construction.construction_management.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class EngineerInvitationRequest {

    @NotNull(message = "Engineer is required")
    @Positive(message = "Engineer is required")
    Integer engineerId;

    @NotBlank(message = "Scope is required")
    @Size(max = 2000, message = "Scope is too long")
    String scope;

    @NotNull(message = "Start date is required")
    LocalDate startDate;

    @NotNull(message = "End date is required")
    LocalDate endDate;

    @Size(max = 1000, message = "Fee terms are too long")
    String feeTerms;

    @Size(max = 255, message = "Permissions are too long")
    String requestedPermissions;

    LocalDate responseDeadline;
}
