package com.construction.construction_management.dto;

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

    double daysWorked;
    double overtimeHours;
    double totalEarned;
    double totalAdvance;
    double totalPaid;
    /** earned - advance - paid (negative means the worker has been overpaid). */
    double pending;
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
        double dailyRate;
        double days;
        double overtimeHours;
        double earned;
        double advance;
        double paid;
        double pending;
    }
}
