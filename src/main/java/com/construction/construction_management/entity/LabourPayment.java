package com.construction.construction_management.entity;

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
public class LabourPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;

    @ManyToOne
    LabourAssignment labourAssignment;

    double amount;

    LocalDate paymentDate;

    @Enumerated(EnumType.STRING)
    PaymentMode mode;

    /** WAGE for normal wage payments, ADVANCE for money given in advance. Null on old rows = WAGE. */
    @Enumerated(EnumType.STRING)
    LabourPaymentType type;

    String remarks;

}