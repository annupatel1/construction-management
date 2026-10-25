package com.construction.construction_management.dto;

import com.construction.construction_management.entity.AttendanceStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class AttendanceRequest {
    @NotNull(message = "Date is required")
    LocalDate date;
    @NotNull(message = "Status is required")
    AttendanceStatus status;
    @PositiveOrZero(message = "Overtime hours cannot be negative")
    @NotNull(message = "overtimeHours is required")
    BigDecimal overtimeHours = BigDecimal.ZERO;
}
