package com.construction.construction_management.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "goods_receipt")
public class GoodsReceipt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "purchase_order_id", nullable = false)
    PurchaseOrder purchaseOrder;

    @Column(name = "received_quantity", nullable = false, precision = 14, scale = 3)
    BigDecimal receivedQuantity;

    @Column(name = "received_date", nullable = false)
    LocalDate receivedDate;

    @Column(name = "unit_price", nullable = false, precision = 14, scale = 2)
    BigDecimal unitPrice;

    @Column(nullable = false, precision = 14, scale = 2)
    BigDecimal amount;

    @Column(length = 100)
    String invoiceNumber;

    @Column(length = 1000)
    String notes;

    @ManyToOne(optional = false)
    @JoinColumn(name = "received_by_id", nullable = false)
    User receivedBy;

    @Column(name = "expense_id")
    Integer expenseId;

    @Column(nullable = false)
    LocalDateTime createdAt;
}
