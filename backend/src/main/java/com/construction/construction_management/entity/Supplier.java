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

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "supplier")
public class Supplier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    Project project;

    @Column(nullable = false, length = 255)
    String name;

    @Column(length = 20)
    String phone;

    @Column(length = 500)
    String address;

    @Column(length = 1000)
    String notes;

    @Column(nullable = false)
    boolean active;

    @Column(nullable = false)
    LocalDateTime createdAt;

    @Column(nullable = false)
    LocalDateTime updatedAt;
}
