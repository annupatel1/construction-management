package com.construction.construction_management.service;

import org.springframework.data.domain.Pageable;

import com.construction.construction_management.dto.LabourAssignmentRequest;
import com.construction.construction_management.dto.LabourAssignmentResponse;
import com.construction.construction_management.dto.LabourAssignmentUpdateRequest;
import com.construction.construction_management.entity.AssignmentStatus;

import java.util.List;

public interface LabourAssignmentService {
    LabourAssignmentResponse assignLabour(int labourId, int projectId, LabourAssignmentRequest request);
    LabourAssignmentResponse updateAssignment(int assignmentId, LabourAssignmentUpdateRequest request);
    LabourAssignmentResponse updateStatus(int assignmentId, AssignmentStatus status);
    List<LabourAssignmentResponse> getAssignmentsByProject(int projectId, Pageable pageable);
    List<LabourAssignmentResponse> getMyAssignments(Pageable pageable);
}
