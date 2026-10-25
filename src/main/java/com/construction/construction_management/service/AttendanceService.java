package com.construction.construction_management.service;

import com.construction.construction_management.dto.AttendanceRequest;
import com.construction.construction_management.dto.AttendanceResponse;

import java.util.List;

public interface AttendanceService {
    AttendanceResponse markAttendance(int assignmentId, AttendanceRequest request);
    List<AttendanceResponse> getAttendanceByAssignment(int assignmentId);
}
