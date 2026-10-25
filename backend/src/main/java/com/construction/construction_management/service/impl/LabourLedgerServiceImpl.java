package com.construction.construction_management.service.impl;

import com.construction.construction_management.dto.LabourLedgerResponse;
import com.construction.construction_management.entity.Attendance;
import com.construction.construction_management.entity.AttendanceStatus;
import com.construction.construction_management.entity.AssignmentStatus;
import com.construction.construction_management.entity.LabourAssignment;
import com.construction.construction_management.entity.LabourPayment;
import com.construction.construction_management.entity.LabourPaymentType;
import com.construction.construction_management.entity.User;
import com.construction.construction_management.repository.AttendanceRepository;
import com.construction.construction_management.repository.LabourAssignmentRepository;
import com.construction.construction_management.repository.LabourPaymentRepository;
import com.construction.construction_management.security.CurrentUser;
import com.construction.construction_management.service.LabourLedgerService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class LabourLedgerServiceImpl implements LabourLedgerService {

    private static final double OVERTIME_DIVISOR = 8;

    private final LabourAssignmentRepository assignmentRepository;
    private final AttendanceRepository attendanceRepository;
    private final LabourPaymentRepository paymentRepository;
    private final CurrentUser currentUser;

    public LabourLedgerServiceImpl(LabourAssignmentRepository assignmentRepository,
                                   AttendanceRepository attendanceRepository,
                                   LabourPaymentRepository paymentRepository,
                                   CurrentUser currentUser) {
        this.assignmentRepository = assignmentRepository;
        this.attendanceRepository = attendanceRepository;
        this.paymentRepository = paymentRepository;
        this.currentUser = currentUser;
    }

    @Override
    public List<LabourLedgerResponse> getLedger() {
        List<LabourAssignment> assignments = assignmentRepository.findByProjectContractorId(currentUser.id());

        // Preserve first-seen order by worker, then sort the final rows by pending money.
        Map<Integer, WorkerTotals> byWorker = new LinkedHashMap<>();

        for (LabourAssignment assignment : assignments) {
            WorkerTotals totals = byWorker.computeIfAbsent(
                    assignment.getLabour().getId(), id -> new WorkerTotals(assignment.getLabour()));

            List<Attendance> attendance = attendanceRepository.findByLabourAssignmentId(assignment.getId());
            List<LabourPayment> payments = paymentRepository.findByLabourAssignmentId(assignment.getId());

            double days = 0;
            double overtimeHours = 0;
            for (Attendance a : attendance) {
                days += dayValue(a.getStatus());
                if (a.getStatus() == AttendanceStatus.OVERTIME) {
                    overtimeHours += a.getOvertimeHours();
                }
            }

            double rate = assignment.getDailyRate();
            double earned = days * rate + overtimeHours * (rate / OVERTIME_DIVISOR);
            double advancePaidLater = 0;
            double paid = 0;
            for (LabourPayment p : payments) {
                if (p.getType() == LabourPaymentType.ADVANCE) {
                    advancePaidLater += p.getAmount();
                } else {
                    paid += p.getAmount();
                }
            }

            double advance = assignment.getAdvanceTaken() + advancePaidLater;
            double pending = earned - advance - paid;

            LabourLedgerResponse.Site site = new LabourLedgerResponse.Site(
                    assignment.getId(),
                    assignment.getProject().getId(),
                    assignment.getProject().getTitle(),
                    assignment.getProject().getLocation(),
                    assignment.getStatus().toString(),
                    rate,
                    days,
                    overtimeHours,
                    earned,
                    advance,
                    paid,
                    pending
            );

            totals.sites.add(site);
            totals.days += days;
            totals.overtimeHours += overtimeHours;
            totals.earned += earned;
            totals.advance += advance;
            totals.paid += paid;
            totals.pending += pending;
            if (assignment.getStatus() == AssignmentStatus.ASSIGNED) {
                totals.activeSites++;
            }
        }

        return byWorker.values().stream()
                .map(WorkerTotals::toResponse)
                .sorted(Comparator.comparingDouble(LabourLedgerResponse::getPending).reversed()
                        .thenComparing(LabourLedgerResponse::getName))
                .toList();
    }

    private static double dayValue(AttendanceStatus status) {
        if (status == AttendanceStatus.FULL_DAY || status == AttendanceStatus.OVERTIME) return 1;
        if (status == AttendanceStatus.HALF_DAY) return 0.5;
        return 0;
    }

    private static final class WorkerTotals {
        final User labour;
        final List<LabourLedgerResponse.Site> sites = new ArrayList<>();
        double days;
        double overtimeHours;
        double earned;
        double advance;
        double paid;
        double pending;
        int activeSites;

        WorkerTotals(User labour) {
            this.labour = labour;
        }

        LabourLedgerResponse toResponse() {
            return new LabourLedgerResponse(
                    labour.getId(),
                    labour.getName(),
                    labour.getPhone(),
                    labour.getAddress(),
                    labour.isPhoneVerified(),
                    days,
                    overtimeHours,
                    earned,
                    advance,
                    paid,
                    pending,
                    activeSites,
                    sites
            );
        }
    }
}
