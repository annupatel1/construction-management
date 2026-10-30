package com.construction.construction_management.controller;

import com.construction.construction_management.dto.LabourAssignmentRequest;
import com.construction.construction_management.dto.LabourAssignmentResponse;
import com.construction.construction_management.dto.LabourAssignmentUpdateRequest;
import com.construction.construction_management.dto.AssignmentStatusRequest;
import com.construction.construction_management.util.Pages;
import com.construction.construction_management.service.LabourAssignmentService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assignments")
public class LabourAssignmentController {

    private final LabourAssignmentService labourAssignmentService;

    public LabourAssignmentController(LabourAssignmentService labourAssignmentService) {
        this.labourAssignmentService = labourAssignmentService;
    }

    @PreAuthorize("hasRole('CONTRACTOR')")
    @PostMapping("/{labourId}/{projectId}")
    public LabourAssignmentResponse assignLabour(@PathVariable int labourId,
                                                 @PathVariable int projectId,
                                                 @Valid @RequestBody LabourAssignmentRequest request) {
        return labourAssignmentService.assignLabour(labourId, projectId, request);
    }

    /** Change the daily rate or the opening advance of an assignment. */
    @PreAuthorize("hasRole('CONTRACTOR')")
    @PatchMapping("/{assignmentId}")
    public LabourAssignmentResponse updateAssignment(@PathVariable int assignmentId,
                                                     @Valid @RequestBody LabourAssignmentUpdateRequest request) {
        return labourAssignmentService.updateAssignment(assignmentId, request);
    }

    /** Mark a worker as completed / removed on a site. */
    @PreAuthorize("hasRole('CONTRACTOR')")
    @PatchMapping("/{assignmentId}/status")
    public LabourAssignmentResponse updateStatus(@PathVariable int assignmentId,
                                                 @Valid @RequestBody AssignmentStatusRequest request) {
        return labourAssignmentService.updateStatus(assignmentId, request.getStatus());
    }

    @GetMapping("/project/{projectId}")
    public List<LabourAssignmentResponse> getAssignmentsByProject(
            @PathVariable int projectId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return labourAssignmentService.getAssignmentsByProject(projectId, Pages.of(page, size));
    }

    /** Assignments relevant to the logged-in user. */
    @GetMapping
    public List<LabourAssignmentResponse> getMyAssignments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return labourAssignmentService.getMyAssignments(Pages.of(page, size));
    }
}
