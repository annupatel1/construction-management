package com.construction.construction_management.service.impl;

import com.construction.construction_management.dto.AttendanceRequest;
import com.construction.construction_management.dto.AttendanceResponse;
import com.construction.construction_management.entity.Attendance;
import com.construction.construction_management.entity.AttendanceStatus;
import com.construction.construction_management.entity.AssignmentStatus;
import com.construction.construction_management.entity.LabourAssignment;
import com.construction.construction_management.exception.BadRequestException;
import com.construction.construction_management.exception.ConflictException;
import com.construction.construction_management.repository.AttendanceRepository;
import com.construction.construction_management.service.AccessGuard;
import com.construction.construction_management.service.AuditService;
import com.construction.construction_management.service.AttendanceService;
import org.springframework.stereotype.Service;
import com.construction.construction_management.util.Money;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final AccessGuard guard;
    private final AuditService auditService;

    public AttendanceServiceImpl(AttendanceRepository attendanceRepository, AccessGuard guard,
                                 AuditService auditService) {
        this.attendanceRepository = attendanceRepository;
        this.guard = guard;
        this.auditService = auditService;
    }

    private AttendanceResponse toResponse(Attendance a) {
        return new AttendanceResponse(
                a.getId(),
                a.getLabourAssignment().getId(),
                a.getDate(),
                a.getStatus().toString(),
                a.getOvertimeHours()
        );
    }

    @Override
    public AttendanceResponse markAttendance(int assignmentId, AttendanceRequest request) {
        LabourAssignment assignment = guard.assignmentForContractor(assignmentId);

        if (assignment.getStatus() != AssignmentStatus.ASSIGNED) {
            throw new BadRequestException("Attendance can only be marked for active assignments");
        }
        if (request.getDate().isAfter(LocalDate.now())) {
            throw new BadRequestException("Attendance cannot be marked for a future date");
        }
        if (attendanceRepository.existsByLabourAssignmentIdAndDate(assignmentId, request.getDate())) {
            throw new ConflictException("Attendance is already marked for this date");
        }

        Attendance attendance = new Attendance();
        attendance.setLabourAssignment(assignment);
        attendance.setDate(request.getDate());
        attendance.setStatus(request.getStatus());
        // overtime hours only make sense on an OVERTIME day
        attendance.setOvertimeHours(request.getStatus() == AttendanceStatus.OVERTIME
                ? Money.money(request.getOvertimeHours()) : BigDecimal.ZERO);

        Attendance saved = attendanceRepository.save(attendance);
        auditService.record("ATTENDANCE_MARKED", "Attendance", saved.getId(),
                "assignmentId=" + assignmentId + "; date=" + saved.getDate()
                        + "; status=" + saved.getStatus() + "; overtimeHours=" + saved.getOvertimeHours());
        return toResponse(saved);
    }

    @Override
    public List<AttendanceResponse> getAttendanceByAssignment(int assignmentId) {
        guard.assignmentForAttendance(assignmentId);
        return attendanceRepository.findByLabourAssignmentId(assignmentId).stream()
                .map(this::toResponse)
                .toList();
    }
}
