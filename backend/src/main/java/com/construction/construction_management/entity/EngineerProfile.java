package com.construction.construction_management.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/** Public engineer profile. Contact details stay on the user and are never returned by the directory. */
@Getter
@Setter
@Entity
@Table(name = "engineer_profile")
public class EngineerProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    User user;

    @Column(nullable = false, length = 100)
    String specialty;

    @Column(length = 500)
    String skills;

    @Column(nullable = false)
    int experienceYears;

    @Column(length = 100)
    String city;

    @Column(length = 255)
    String serviceArea;

    @Column(length = 255)
    String languages;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    EngineerAvailability availability = EngineerAvailability.AVAILABLE;

    @Column(length = 2000)
    String bio;

    @Column(length = 500)
    String portfolioUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    CredentialStatus credentialStatus = CredentialStatus.UNVERIFIED;

    @Column(nullable = false)
    LocalDateTime updatedAt;
}
