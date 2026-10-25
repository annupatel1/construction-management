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
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;

    @ManyToOne
    User houseOwner;

    @ManyToOne
    User contractor;

    String title;
    String location;
    double areaSqFt;
    double ratePerSqFt;
    double totalEstimatedCost;

    @Enumerated(EnumType.STRING)
    ContractType contractType;

    @Enumerated(EnumType.STRING)
    ProjectStatus status;

    LocalDate startDate;
    LocalDate endDate;

}