package com.construction.construction_management.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/** One labourer's money picture across every site the contractor has put them on. */
@Getter
@Setter
@AllArgsConstructor
public class LabourLedgerResponse {

    int labourId;
    String name;
    String phone;
    String address;
    /** true when the worker has registered in the app (can log in and see their own work). */
    boolean registered;

    BigDecimal daysWorked;
    BigDecimal overtimeHours;
    BigDecimal totalEarned;
    BigDecimal totalAdvance;
    BigDecimal totalPaid;
    /** earned - advance - paid (negative means the worker has been overpaid). */
    BigDecimal pending;
    int activeSites;
    List<Site> sites;

    @Getter
    @Setter
    @AllArgsConstructor
    public static class Site {
        int assignmentId;
        int projectId;
        String projectTitle;
        String projectLocation;
        String status;
        BigDecimal dailyRate;
        BigDecimal days;
        BigDecimal overtimeHours;
        BigDecimal earned;
        BigDecimal advance;
        BigDecimal paid;
        BigDecimal pending;
    }
}
