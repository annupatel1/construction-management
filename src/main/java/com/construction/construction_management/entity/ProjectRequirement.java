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
public class ProjectRequirement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;

    @ManyToOne
    User houseOwner;

    String city;
    String area;
    String pincode;

    double areaSqFt;

    @Enumerated(EnumType.STRING)
    ContractType contractType;

    String description;

    @Enumerated(EnumType.STRING)
    RequirementStatus status;

    LocalDate postedDate;

}