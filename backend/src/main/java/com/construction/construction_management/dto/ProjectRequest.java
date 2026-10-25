package com.construction.construction_management.dto;

import com.construction.construction_management.entity.ContractType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class ProjectRequest {
    @NotBlank(message = "Title is required")
    String title;
    @NotBlank(message = "Location is required")
    String location;
    @Positive(message = "Area must be greater than 0")
    @NotNull(message = "areaSqFt is required")
    BigDecimal areaSqFt;
    @Positive(message = "Rate per sq ft must be greater than 0")
    @NotNull(message = "ratePerSqFt is required")
    BigDecimal ratePerSqFt;
    @NotNull(message = "Contract type is required")
    ContractType contractType;
    @NotNull(message = "Start date is required")
    LocalDate startDate;
    LocalDate endDate;
}
