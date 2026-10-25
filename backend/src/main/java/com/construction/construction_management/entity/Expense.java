package com.construction.construction_management.entity;

import java.math.BigDecimal;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;

    @ManyToOne
    Project project;

    @Enumerated(EnumType.STRING)
    MaterialCategory category;

    @Column(precision = 14, scale = 2)
    BigDecimal amount;

    @Column(precision = 14, scale = 3)
    BigDecimal quantity;

    String unit;

    LocalDate purchaseDate;

}