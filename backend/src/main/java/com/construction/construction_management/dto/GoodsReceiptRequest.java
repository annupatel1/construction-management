package com.construction.construction_management.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class GoodsReceiptRequest {

    @NotNull(message = "Received quantity is required")
    @Positive(message = "Received quantity must be greater than 0")
    BigDecimal receivedQuantity;

    @NotNull(message = "Received date is required")
    LocalDate receivedDate;

    @Size(max = 100, message = "Invoice number is too long")
    String invoiceNumber;

    @Size(max = 1000, message = "Notes are too long")
    String notes;
}
