package com.construction.construction_management.dto;

import com.construction.construction_management.entity.Supplier;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SupplierResponse {

    int id;
    String name;
    String phone;
    String address;
    String notes;
    boolean active;

    public static SupplierResponse from(Supplier supplier) {
        return new SupplierResponse(
                supplier.getId(),
                supplier.getName(),
                supplier.getPhone(),
                supplier.getAddress(),
                supplier.getNotes(),
                supplier.isActive());
    }
}
