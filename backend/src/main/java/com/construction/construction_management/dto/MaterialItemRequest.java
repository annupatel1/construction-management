package com.construction.construction_management.dto;

import com.construction.construction_management.entity.MaterialCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class MaterialItemRequest {

    @NotBlank(message = "Material name is required")
    @Size(max = 255, message = "Material name is too long")
    String name;

    @NotNull(message = "Material category is required")
    MaterialCategory category;

    @NotBlank(message = "Unit is required")
    @Size(max = 32, message = "Unit is too long")
    String unit;

    @PositiveOrZero(message = "Minimum stock cannot be negative")
    BigDecimal minimumStock;

    Boolean active;
}
