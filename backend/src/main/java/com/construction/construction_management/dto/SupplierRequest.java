package com.construction.construction_management.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SupplierRequest {

    @NotBlank(message = "Supplier name is required")
    @Size(max = 255, message = "Supplier name is too long")
    String name;

    @Size(max = 20, message = "Supplier phone is too long")
    String phone;

    @Size(max = 500, message = "Supplier address is too long")
    String address;

    @Size(max = 1000, message = "Supplier notes are too long")
    String notes;

    Boolean active;
}
