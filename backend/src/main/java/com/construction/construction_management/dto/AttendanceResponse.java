package com.construction.construction_management.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
public class AttendanceResponse {

    int id;
    int labourAssignmentId;
    LocalDate date;
    String status;
    BigDecimal overtimeHours;

}