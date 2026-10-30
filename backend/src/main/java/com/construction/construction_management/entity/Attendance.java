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
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@UniqueConstraint(columnNames = {"labourAssignment_id", "date"}, name = "uk_attendance_labour_date")
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;

    @ManyToOne
    LabourAssignment labourAssignment;

    LocalDate date;

    @Enumerated(EnumType.STRING)
    AttendanceStatus status;

    @Column(precision = 6, scale = 2)
    BigDecimal overtimeHours;

}