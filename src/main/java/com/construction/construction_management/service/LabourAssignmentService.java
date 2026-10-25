package com.construction.construction_management.service;

import com.construction.construction_management.dto.LabourAssignmentRequest;
import com.construction.construction_management.dto.LabourAssignmentResponse;

import java.util.List;

public interface LabourAssignmentService {
    LabourAssignmentResponse assignLabour(int labourId, int projectId, LabourAssignmentRequest request);
    List<LabourAssignmentResponse> getAssignmentsByProject(int projectId);
    List<LabourAssignmentResponse> getMyAssignments();
}
