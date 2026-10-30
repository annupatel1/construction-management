package com.construction.construction_management.dto;

import com.construction.construction_management.entity.ContractType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProjectRequirementRequest {
    @NotBlank(message = "City is required")
    String city;
    @NotBlank(message = "Area is required")
    String area;
    @NotBlank(message = "Pincode is required")
    @Pattern(regexp = "^[1-9]\\d{5}$", message = "Enter a valid 6-digit pincode")
    String pincode;
    @Positive(message = "Area sq ft must be greater than 0")
    @NotNull(message = "areaSqFt is required")
    BigDecimal areaSqFt;
    @NotNull(message = "Contract type is required")
    ContractType contractType;
    String description;
}
