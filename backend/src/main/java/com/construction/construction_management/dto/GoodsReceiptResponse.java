package com.construction.construction_management.dto;

import com.construction.construction_management.entity.GoodsReceipt;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class GoodsReceiptResponse {

    int id;
    BigDecimal receivedQuantity;
    LocalDate receivedDate;
    BigDecimal unitPrice;
    BigDecimal amount;
    String invoiceNumber;
    String notes;
    SitePartyResponse receivedBy;
    Integer expenseId;
    LocalDateTime createdAt;

    public static GoodsReceiptResponse from(GoodsReceipt receipt) {
        return new GoodsReceiptResponse(
                receipt.getId(),
                receipt.getReceivedQuantity(),
                receipt.getReceivedDate(),
                receipt.getUnitPrice(),
                receipt.getAmount(),
                receipt.getInvoiceNumber(),
                receipt.getNotes(),
                SitePartyResponse.from(receipt.getReceivedBy()),
                receipt.getExpenseId(),
                receipt.getCreatedAt());
    }
}
