package com.construction.construction_management.service.impl;

import com.construction.construction_management.entity.Attendance;
import com.construction.construction_management.entity.AttendanceStatus;
import com.construction.construction_management.entity.Expense;
import com.construction.construction_management.entity.LabourAssignment;
import com.construction.construction_management.entity.LabourPayment;
import com.construction.construction_management.entity.LabourPaymentType;
import com.construction.construction_management.entity.MaterialItem;
import com.construction.construction_management.entity.Milestone;
import com.construction.construction_management.entity.Payment;
import com.construction.construction_management.entity.ProgressReport;
import com.construction.construction_management.entity.ProgressReportStatus;
import com.construction.construction_management.entity.Project;
import com.construction.construction_management.entity.ProjectIssue;
import com.construction.construction_management.entity.ProjectTask;
import com.construction.construction_management.entity.RoleName;
import com.construction.construction_management.entity.StockMovement;
import com.construction.construction_management.entity.StockMovementType;
import com.construction.construction_management.repository.AttendanceRepository;
import com.construction.construction_management.repository.ExpenseRepository;
import com.construction.construction_management.repository.LabourAssignmentRepository;
import com.construction.construction_management.repository.LabourPaymentRepository;
import com.construction.construction_management.repository.MaterialItemRepository;
import com.construction.construction_management.repository.MilestoneRepository;
import com.construction.construction_management.repository.PaymentRepository;
import com.construction.construction_management.repository.ProgressReportRepository;
import com.construction.construction_management.repository.ProjectIssueRepository;
import com.construction.construction_management.repository.ProjectTaskRepository;
import com.construction.construction_management.repository.StockMovementRepository;
import com.construction.construction_management.security.CurrentUser;
import com.construction.construction_management.service.AccessGuard;
import com.construction.construction_management.service.ReportExportService;
import com.construction.construction_management.util.Csv;
import com.construction.construction_management.util.Money;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportExportServiceImpl implements ReportExportService {

    private static final BigDecimal OVERTIME_DIVISOR = BigDecimal.valueOf(8);
    private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final AccessGuard guard;
    private final CurrentUser currentUser;
    private final PaymentRepository paymentRepository;
    private final ExpenseRepository expenseRepository;
    private final LabourPaymentRepository labourPaymentRepository;
    private final LabourAssignmentRepository assignmentRepository;
    private final AttendanceRepository attendanceRepository;
    private final MaterialItemRepository materialRepository;
    private final StockMovementRepository movementRepository;
    private final ProgressReportRepository reportRepository;
    private final ProjectTaskRepository taskRepository;
    private final MilestoneRepository milestoneRepository;
    private final ProjectIssueRepository issueRepository;

    public ReportExportServiceImpl(AccessGuard guard,
                                   CurrentUser currentUser,
                                   PaymentRepository paymentRepository,
                                   ExpenseRepository expenseRepository,
                                   LabourPaymentRepository labourPaymentRepository,
                                   LabourAssignmentRepository assignmentRepository,
                                   AttendanceRepository attendanceRepository,
                                   MaterialItemRepository materialRepository,
                                   StockMovementRepository movementRepository,
                                   ProgressReportRepository reportRepository,
                                   ProjectTaskRepository taskRepository,
                                   MilestoneRepository milestoneRepository,
                                   ProjectIssueRepository issueRepository) {
        this.guard = guard;
        this.currentUser = currentUser;
        this.paymentRepository = paymentRepository;
        this.expenseRepository = expenseRepository;
        this.labourPaymentRepository = labourPaymentRepository;
        this.assignmentRepository = assignmentRepository;
        this.attendanceRepository = attendanceRepository;
        this.materialRepository = materialRepository;
        this.movementRepository = movementRepository;
        this.reportRepository = reportRepository;
        this.taskRepository = taskRepository;
        this.milestoneRepository = milestoneRepository;
        this.issueRepository = issueRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public String financeCsv(int projectId) {
        Project project = guard.projectForOwnerOrContractor(projectId);
        RoleName role = currentUser.role();
        boolean includeLabour = role == RoleName.CONTRACTOR || role == RoleName.ADMIN;

        List<Payment> payments = paymentRepository.findByProjectId(projectId);
        List<Expense> expenses = expenseRepository.findByProjectId(projectId);

        List<FinanceRow> rows = new ArrayList<>();
        for (Payment payment : payments) {
            rows.add(new FinanceRow(payment.getPaymentDate(), "Owner payment", "Contractor payment",
                    payment.getRemarks(), Money.nvl(payment.getAmount()),
                    payment.getMode() == null ? "" : payment.getMode().name()));
        }
        for (Expense expense : expenses) {
            String description = expense.getQuantity() == null
                    ? null : expense.getQuantity().toPlainString() + " " + safe(expense.getUnit());
            rows.add(new FinanceRow(expense.getPurchaseDate(), "Expense",
                    expense.getCategory() == null ? "" : expense.getCategory().name(),
                    description, Money.nvl(expense.getAmount()), safe(expense.getUnit())));
        }
        if (includeLabour) {
            for (LabourPayment payment : labourPaymentRepository.findAllForProjectExport(projectId)) {
                LabourPaymentType type = payment.getType() == null ? LabourPaymentType.WAGE : payment.getType();
                rows.add(new FinanceRow(payment.getPaymentDate(),
                        "Labour payment", type.name(),
                        join(payment.getLabourAssignment().getLabour().getName(), payment.getRemarks()),
                        Money.nvl(payment.getAmount()),
                        payment.getMode() == null ? "" : payment.getMode().name()));
            }
        }
        rows.sort(Comparator.comparing(FinanceRow::date, Comparator.nullsLast(LocalDate::compareTo))
                .thenComparing(FinanceRow::type));

        List<String> lines = new ArrayList<>();
        metadata(lines, project, "Finance export");
        lines.add(Csv.row("Date", "Type", "Category", "Description", "Amount (INR)", "Mode/Unit"));
        BigDecimal paymentsTotal = BigDecimal.ZERO;
        BigDecimal expensesTotal = BigDecimal.ZERO;
        BigDecimal labourTotal = BigDecimal.ZERO;
        for (FinanceRow row : rows) {
            lines.add(Csv.row(row.date(), row.type(), row.category(), row.description(),
                    Money.money(row.amount()), row.mode()));
            if ("Owner payment".equals(row.type())) {
                paymentsTotal = Money.add(paymentsTotal, row.amount());
            } else if ("Expense".equals(row.type())) {
                expensesTotal = Money.add(expensesTotal, row.amount());
            } else {
                labourTotal = Money.add(labourTotal, row.amount());
            }
        }
        lines.add("");
        lines.add(Csv.row("SUMMARY", "Owner payments", "", "", Money.money(paymentsTotal), ""));
        lines.add(Csv.row("SUMMARY", "Material expenses", "", "", Money.money(expensesTotal), ""));
        if (includeLabour) {
            lines.add(Csv.row("SUMMARY", "Labour payments", "", "", Money.money(labourTotal), ""));
        }
        return Csv.document(lines);
    }

    @Override
    @Transactional(readOnly = true)
    public String attendanceCsv(int projectId) {
        Project project = guard.projectForOwnerOrContractor(projectId);
        RoleName role = currentUser.role();
        if (role != RoleName.CONTRACTOR && role != RoleName.ADMIN) {
            throw new AccessDeniedException("Only the contractor or an admin can export attendance");
        }

        List<AttendanceRow> rows = new ArrayList<>();
        BigDecimal totalDays = BigDecimal.ZERO;
        BigDecimal totalOvertime = BigDecimal.ZERO;
        BigDecimal totalEarned = BigDecimal.ZERO;

        for (LabourAssignment assignment : assignmentRepository.findByProjectId(projectId)) {
            BigDecimal rate = Money.nvl(assignment.getDailyRate());
            BigDecimal overtimeRate = rate.divide(OVERTIME_DIVISOR, 4, RoundingMode.HALF_UP);
            for (Attendance attendance : attendanceRepository.findByLabourAssignmentId(assignment.getId())) {
                BigDecimal days = dayValue(attendance.getStatus());
                BigDecimal overtime = attendance.getStatus() == AttendanceStatus.OVERTIME
                        ? Money.nvl(attendance.getOvertimeHours()) : BigDecimal.ZERO;
                BigDecimal earned = Money.add(days.multiply(rate), overtime.multiply(overtimeRate));
                rows.add(new AttendanceRow(
                        assignment.getLabour().getName(),
                        safe(assignment.getLabour().getPhone()),
                        rate,
                        attendance.getDate(),
                        attendance.getStatus() == null ? "" : attendance.getStatus().name(),
                        overtime,
                        days,
                        earned,
                        assignment.getStatus() == null ? "" : assignment.getStatus().name()));
                totalDays = totalDays.add(days);
                totalOvertime = totalOvertime.add(overtime);
                totalEarned = Money.add(totalEarned, earned);
            }
        }
        rows.sort(Comparator.comparing(AttendanceRow::labour)
                .thenComparing(AttendanceRow::date, Comparator.nullsLast(LocalDate::compareTo)));

        List<String> lines = new ArrayList<>();
        metadata(lines, project, "Attendance export");
        lines.add(Csv.row("Labour", "Phone", "Daily rate (INR)", "Date", "Status",
                "Overtime hours", "Days", "Earned (INR)", "Assignment status"));
        for (AttendanceRow row : rows) {
            lines.add(Csv.row(row.labour(), row.phone(), row.rate(), row.date(), row.status(),
                    row.overtime(), row.days(), row.earned(), row.assignmentStatus()));
        }
        lines.add("");
        lines.add(Csv.row("TOTAL", "", "", "", "", totalOvertime, totalDays, Money.money(totalEarned), ""));
        return Csv.document(lines);
    }

    @Override
    @Transactional(readOnly = true)
    public String stockCsv(int projectId) {
        Project project = guard.projectForOwnerOrContractor(projectId);
        List<MaterialItem> materials = materialRepository.findAllByProjectIdOrderByNameAscIdAsc(projectId);
        List<StockMovement> movements = movementRepository.findAllForProjectExport(projectId);

        Map<Integer, BigDecimal> finalBalances = new HashMap<>();
        for (MaterialItem material : materials) {
            finalBalances.put(material.getId(), BigDecimal.ZERO);
        }
        for (StockMovement movement : movements) {
            BigDecimal signed = signedQuantity(movement);
            finalBalances.merge(movement.getMaterial().getId(), signed, BigDecimal::add);
        }

        List<String> lines = new ArrayList<>();
        metadata(lines, project, "Stock export");
        lines.add(Csv.row("Material", "Category", "Unit", "Active", "Minimum stock",
                "Current balance", "Low stock"));
        for (MaterialItem material : materials) {
            BigDecimal balance = Money.quantity(finalBalances.getOrDefault(material.getId(), BigDecimal.ZERO));
            boolean low = material.isActive()
                    && balance.compareTo(Money.quantity(material.getMinimumStock())) < 0;
            lines.add(Csv.row(material.getName(), material.getCategory(), material.getUnit(),
                    material.isActive(), Money.quantity(material.getMinimumStock()), balance, low));
        }
        lines.add("");
        lines.add(Csv.row("Date", "Material", "Unit", "Type", "Quantity", "Unit cost (INR)",
                "Reference", "Notes", "Balance after"));
        Map<Integer, BigDecimal> running = new HashMap<>();
        for (MaterialItem material : materials) {
            running.put(material.getId(), BigDecimal.ZERO);
        }
        for (StockMovement movement : movements) {
            BigDecimal balance = Money.quantity(running.merge(
                    movement.getMaterial().getId(), signedQuantity(movement), BigDecimal::add));
            String reference = safe(movement.getReferenceType())
                    + (movement.getReferenceId() == null ? "" : " #" + movement.getReferenceId());
            lines.add(Csv.row(movement.getMovementDate(), movement.getMaterial().getName(),
                    movement.getMaterial().getUnit(), movement.getMovementType(),
                    Money.quantity(movement.getQuantity()), movement.getUnitCost(),
                    reference.trim(), movement.getNotes(), balance));
        }
        return Csv.document(lines);
    }

    @Override
    @Transactional(readOnly = true)
    public String siteCsv(int projectId) {
        Project project = guard.projectForSiteRead(projectId);
        boolean owner = currentUser.role() == RoleName.HOUSE_OWNER;

        List<ProgressReport> reports = reportRepository
                .findByProjectIdOrderByReportDateDescIdDesc(projectId);
        if (owner) {
            reports = reports.stream()
                    .filter(report -> report.getStatus() == ProgressReportStatus.PUBLISHED)
                    .toList();
        }
        List<ProjectTask> tasks = taskRepository.findByProjectIdOrderByDueDateAscIdDesc(projectId);
        List<Milestone> milestones = milestoneRepository.findByProjectIdOrderByTargetDateAscIdAsc(projectId);
        List<ProjectIssue> issues = issueRepository.findByProjectIdOrderByCreatedAtDescIdDesc(projectId);

        List<String> lines = new ArrayList<>();
        metadata(lines, project, "Site operations export");
        lines.add(Csv.row("Type", "Date", "Title", "Status", "Person", "Details"));

        for (ProgressReport report : reports) {
            lines.add(Csv.row("Progress report", report.getReportDate(), "Daily progress report",
                    report.getStatus(), report.getAuthor().getName(),
                    join("Completed: " + report.getCompletedWork(),
                            report.getBlockers() == null ? null : "Blockers: " + report.getBlockers(),
                            report.getNextDayPlan() == null ? null : "Next: " + report.getNextDayPlan())));
        }
        for (ProjectTask task : tasks) {
            LocalDate date = task.getDueDate() != null
                    ? task.getDueDate() : task.getCreatedAt().toLocalDate();
            lines.add(Csv.row("Task", date, task.getTitle(), task.getStatus(),
                    task.getAssignee() == null ? "-" : task.getAssignee().getName(),
                    join(task.getDescription(),
                            task.getDelayReason() == null ? null : "Delay: " + task.getDelayReason())));
        }
        for (Milestone milestone : milestones) {
            lines.add(Csv.row("Milestone", milestone.getTargetDate(), milestone.getTitle(),
                    milestone.getStatus(), "-",
                    join(milestone.getDescription(),
                            milestone.getCompletedDate() == null ? null
                                    : "Completed: " + milestone.getCompletedDate())));
        }
        for (ProjectIssue issue : issues) {
            String person = issue.getAssignee() != null
                    ? issue.getAssignee().getName() : issue.getRaisedBy().getName();
            lines.add(Csv.row("Issue", issue.getCreatedAt().toLocalDate(), issue.getTitle(),
                    issue.getStatus(), person,
                    join(issue.getCategory().name(), issue.getDescription(),
                            issue.getResolutionNote() == null ? null
                                    : "Resolution: " + issue.getResolutionNote())));
        }
        return Csv.document(lines);
    }

    private static BigDecimal signedQuantity(StockMovement movement) {
        BigDecimal quantity = Money.quantity(movement.getQuantity());
        return movement.getMovementType() == StockMovementType.RECEIPT
                || movement.getMovementType() == StockMovementType.RETURN
                ? quantity : quantity.negate();
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

    private static void metadata(List<String> lines, Project project, String report) {
        lines.add(Csv.row("Project", project.getTitle()));
        lines.add(Csv.row("Location", project.getLocation()));
        lines.add(Csv.row("Report", report));
        lines.add(Csv.row("Exported at", LocalDateTime.now().format(TIMESTAMP)));
        lines.add("");
    }

    private static String join(String... values) {
        StringBuilder text = new StringBuilder();
        for (String value : values) {
            if (value == null || value.isBlank()) {
                continue;
            }
            if (text.length() > 0) {
                text.append(" | ");
            }
            text.append(value.trim());
        }
        return text.toString();
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private record FinanceRow(LocalDate date, String type, String category,
                              String description, BigDecimal amount, String mode) {
    }

    private record AttendanceRow(String labour, String phone, BigDecimal rate, LocalDate date,
                                 String status, BigDecimal overtime, BigDecimal days,
                                 BigDecimal earned, String assignmentStatus) {
    }
}
