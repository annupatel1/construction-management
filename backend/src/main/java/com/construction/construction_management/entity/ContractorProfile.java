package com.construction.construction_management.entity;

import java.math.BigDecimal;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class ContractorProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;

    @OneToOne
    User user;

    int experienceYears;
    @Column(precision = 12, scale = 2)
    BigDecimal ratePerSqFt;
    boolean isVerified;
    String bio;

    String city;
    String area;
    String pincode;
    boolean isAvailable;

}