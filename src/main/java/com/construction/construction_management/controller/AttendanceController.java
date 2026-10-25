package com.construction.construction_management.controller;

import com.construction.construction_management.dto.AttendanceRequest;
import com.construction.construction_management.dto.AttendanceResponse;
import com.construction.construction_management.service.AttendanceService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @PreAuthorize("hasRole('CONTRACTOR')")
    @PostMapping("/{assignmentId}")
    public AttendanceResponse markAttendance(@PathVariable int assignmentId,
                                             @Valid @RequestBody AttendanceRequest request) {
        return attendanceService.markAttendance(assignmentId, request);
    }

    @GetMapping("/assignment/{assignmentId}")
    public List<AttendanceResponse> getAttendanceByAssignment(@PathVariable int assignmentId) {
        return attendanceService.getAttendanceByAssignment(assignmentId);
    }

}