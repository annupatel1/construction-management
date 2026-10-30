package com.construction.construction_management.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "stock_movement")
public class StockMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    Project project;

    @ManyToOne(optional = false)
    @JoinColumn(name = "material_id", nullable = false)
    MaterialItem material;

    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", nullable = false, length = 32)
    StockMovementType movementType;

    @Column(nullable = false, precision = 14, scale = 3)
    BigDecimal quantity;

    @Column(name = "unit_cost", precision = 14, scale = 2)
    BigDecimal unitCost;

    @Column(name = "movement_date", nullable = false)
    LocalDate movementDate;

    @Column(name = "reference_type", length = 32)
    String referenceType;

    @Column(name = "reference_id")
    Integer referenceId;

    @Column(length = 1000)
    String notes;

    @ManyToOne(optional = false)
    @JoinColumn(name = "created_by_id", nullable = false)
    User createdBy;

    @Column(nullable = false)
    LocalDateTime createdAt;
}
