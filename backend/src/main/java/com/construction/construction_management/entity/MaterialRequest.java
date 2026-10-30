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
@Table(name = "material_request")
public class MaterialRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    Project project;

    @ManyToOne(optional = false)
    @JoinColumn(name = "material_id", nullable = false)
    MaterialItem material;

    @ManyToOne(optional = false)
    @JoinColumn(name = "requested_by_id", nullable = false)
    User requestedBy;

    @Column(nullable = false, precision = 14, scale = 3)
    BigDecimal quantity;

    LocalDate neededBy;

    @Column(length = 2000)
    String notes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    MaterialRequestStatus status;

    @ManyToOne
    @JoinColumn(name = "reviewed_by_id")
    User reviewedBy;

    LocalDateTime reviewedAt;

    @Column(length = 1000)
    String reviewNote;

    @Column(nullable = false)
    LocalDateTime createdAt;

    @Column(nullable = false)
    LocalDateTime updatedAt;
}
