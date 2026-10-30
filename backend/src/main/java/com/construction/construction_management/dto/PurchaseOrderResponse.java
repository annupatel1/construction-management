package com.construction.construction_management.dto;

import com.construction.construction_management.entity.GoodsReceipt;
import com.construction.construction_management.entity.PurchaseOrder;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class PurchaseOrderResponse {

    int id;
    int projectId;
    int materialId;
    String materialName;
    String materialUnit;
    int supplierId;
    String supplierName;
    Integer materialRequestId;
    BigDecimal quantity;
    BigDecimal unitPrice;
    BigDecimal totalAmount;
    BigDecimal receivedQuantity;
    BigDecimal remainingQuantity;
    String status;
    LocalDate orderedDate;
    LocalDate expectedDate;
    String notes;
    LocalDateTime createdAt;
    List<GoodsReceiptResponse> receipts;

    public static PurchaseOrderResponse from(PurchaseOrder order) {
        return new PurchaseOrderResponse(
                order.getId(),
                order.getProject().getId(),
                order.getMaterial().getId(),
                order.getMaterial().getName(),
                order.getMaterial().getUnit(),
                order.getSupplier().getId(),
                order.getSupplier().getName(),
                order.getMaterialRequest() == null ? null : order.getMaterialRequest().getId(),
                order.getQuantity(),
                order.getUnitPrice(),
                order.getTotalAmount(),
                order.getReceivedQuantity(),
                order.getQuantity().subtract(order.getReceivedQuantity()),
                order.getStatus().name(),
                order.getOrderedDate(),
                order.getExpectedDate(),
                order.getNotes(),
                order.getCreatedAt(),
                order.getReceipts().stream().map(GoodsReceiptResponse::from).toList());
    }
}
