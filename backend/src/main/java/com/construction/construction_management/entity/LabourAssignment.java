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
public class LabourAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;

    @ManyToOne
    User labour;

    @ManyToOne
    Project project;

    @Column(precision = 12, scale = 2)
    BigDecimal dailyRate;

    @Enumerated(EnumType.STRING)
    AssignmentStatus status;

    @Column(precision = 14, scale = 2)
    BigDecimal advanceTaken;

    LocalDate assignedDate;

}