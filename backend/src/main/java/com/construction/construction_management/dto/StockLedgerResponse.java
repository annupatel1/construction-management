package com.construction.construction_management.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class StockLedgerResponse {

    List<MaterialItemResponse> materials;
    List<StockMovementResponse> movements;
    long lowStockCount;
}
