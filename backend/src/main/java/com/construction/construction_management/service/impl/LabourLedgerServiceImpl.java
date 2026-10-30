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
import com.construction.construction_management.util.Money;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class LabourLedgerServiceImpl implements LabourLedgerService {

    private static final BigDecimal OVERTIME_DIVISOR = BigDecimal.valueOf(8);

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

            BigDecimal days = BigDecimal.ZERO;
            BigDecimal overtimeHours = BigDecimal.ZERO;
            for (Attendance a : attendance) {
                days = days.add(dayValue(a.getStatus()));
                if (a.getStatus() == AttendanceStatus.OVERTIME) {
                    overtimeHours = overtimeHours.add(Money.nvl(a.getOvertimeHours()));
                }
            }

            BigDecimal rate = Money.nvl(assignment.getDailyRate());
            BigDecimal overtimeRate = rate.divide(OVERTIME_DIVISOR, 4, RoundingMode.HALF_UP);
            BigDecimal earned = Money.add(days.multiply(rate), overtimeHours.multiply(overtimeRate));
            BigDecimal advancePaidLater = BigDecimal.ZERO;
            BigDecimal paid = BigDecimal.ZERO;
            for (LabourPayment p : payments) {
                if (p.getType() == LabourPaymentType.ADVANCE) {
                    advancePaidLater = Money.add(advancePaidLater, p.getAmount());
                } else {
                    paid = Money.add(paid, p.getAmount());
                }
            }

            BigDecimal advance = Money.add(assignment.getAdvanceTaken(), advancePaidLater);
            BigDecimal pending = Money.subtract(Money.subtract(earned, advance), paid);

            LabourLedgerResponse.Site site = new LabourLedgerResponse.Site(
                    assignment.getId(),
                    assignment.getProject().getId(),
                    assignment.getProject().getTitle(),
                    assignment.getProject().getLocation(),
                    assignment.getStatus().toString(),
                    Money.money(rate),
                    days,
                    overtimeHours,
                    earned,
                    advance,
                    paid,
                    pending
            );

            totals.sites.add(site);
            totals.days = totals.days.add(days);
            totals.overtimeHours = totals.overtimeHours.add(overtimeHours);
            totals.earned = Money.add(totals.earned, earned);
            totals.advance = Money.add(totals.advance, advance);
            totals.paid = Money.add(totals.paid, paid);
            totals.pending = Money.add(totals.pending, pending);
            if (assignment.getStatus() == AssignmentStatus.ASSIGNED) {
                totals.activeSites++;
            }
        }

        return byWorker.values().stream()
                .map(WorkerTotals::toResponse)
                .sorted(Comparator.comparing((LabourLedgerResponse r) -> Money.nvl(r.getPending())).reversed()
                        .thenComparing(LabourLedgerResponse::getName))
                .toList();
    }

    private static BigDecimal dayValue(AttendanceStatus status) {
        if (status == AttendanceStatus.FULL_DAY || status == AttendanceStatus.OVERTIME) {
            return BigDecimal.ONE;
        }
        if (status == AttendanceStatus.HALF_DAY) {
            return new BigDecimal("0.5");
        }
        return BigDecimal.ZERO;
    }

    private static final class WorkerTotals {
        final User labour;
        final List<LabourLedgerResponse.Site> sites = new ArrayList<>();
        BigDecimal days = BigDecimal.ZERO;
        BigDecimal overtimeHours = BigDecimal.ZERO;
        BigDecimal earned = Money.zero();
        BigDecimal advance = Money.zero();
        BigDecimal paid = Money.zero();
        BigDecimal pending = Money.zero();
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
