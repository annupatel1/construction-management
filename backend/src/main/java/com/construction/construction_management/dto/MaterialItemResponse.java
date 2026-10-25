package com.construction.construction_management.dto;

import com.construction.construction_management.entity.MaterialItem;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class MaterialItemResponse {

    int id;
    String name;
    String category;
    String unit;
    BigDecimal minimumStock;
    boolean active;
    BigDecimal balance;
    boolean lowStock;

    public static MaterialItemResponse from(MaterialItem item, BigDecimal balance) {
        BigDecimal safeBalance = balance == null ? BigDecimal.ZERO : balance;
        return new MaterialItemResponse(
                item.getId(),
                item.getName(),
                item.getCategory().name(),
                item.getUnit(),
                item.getMinimumStock(),
                item.isActive(),
                safeBalance,
                safeBalance.compareTo(item.getMinimumStock()) < 0);
    }
}
