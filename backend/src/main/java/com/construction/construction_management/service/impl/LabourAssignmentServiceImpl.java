package com.construction.construction_management.service.impl;

import com.construction.construction_management.dto.LabourAssignmentRequest;
import com.construction.construction_management.dto.LabourAssignmentResponse;
import com.construction.construction_management.dto.LabourAssignmentUpdateRequest;
import com.construction.construction_management.entity.*;
import com.construction.construction_management.exception.BadRequestException;
import com.construction.construction_management.exception.ConflictException;
import com.construction.construction_management.exception.ResourceNotFoundException;
import com.construction.construction_management.repository.LabourAssignmentRepository;
import com.construction.construction_management.repository.UserRepository;
import com.construction.construction_management.security.CurrentUser;
import com.construction.construction_management.service.AuditService;
import com.construction.construction_management.service.AccessGuard;
import com.construction.construction_management.service.LabourAssignmentService;
import org.springframework.stereotype.Service;
import com.construction.construction_management.util.Money;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

@Service
public class LabourAssignmentServiceImpl implements LabourAssignmentService {

    private final LabourAssignmentRepository assignmentRepository;
    private final UserRepository userRepository;
    private final CurrentUser currentUser;
    private final AccessGuard guard;
    private final AuditService auditService;

    public LabourAssignmentServiceImpl(LabourAssignmentRepository assignmentRepository,
                                       UserRepository userRepository,
                                       CurrentUser currentUser,
                                       AccessGuard guard,
                                       AuditService auditService) {
        this.assignmentRepository = assignmentRepository;
        this.userRepository = userRepository;
        this.currentUser = currentUser;
        this.guard = guard;
        this.auditService = auditService;
    }

    @Override
    public LabourAssignmentResponse assignLabour(int labourId, int projectId, LabourAssignmentRequest request) {
        Project project = guard.projectForContractor(projectId);

        User labour = userRepository.findById(labourId)
                .orElseThrow(() -> new ResourceNotFoundException("Labour not found"));
        if (labour.getRole().getName() != RoleName.LABOUR) {
            throw new BadRequestException("Selected user is not a labourer");
        }
        if (assignmentRepository.existsByLabourIdAndProjectIdAndStatus(labourId, projectId, AssignmentStatus.ASSIGNED)) {
            throw new ConflictException("This labourer is already assigned to the project");
        }

        LabourAssignment a = new LabourAssignment();
        a.setLabour(labour);
        a.setProject(project);
        a.setDailyRate(Money.money(request.getDailyRate()));
        a.setStatus(AssignmentStatus.ASSIGNED);
        a.setAdvanceTaken(Money.money(request.getAdvanceTaken()));
        a.setAssignedDate(request.getAssignedDate() != null ? request.getAssignedDate() : LocalDate.now());

        LabourAssignment saved = assignmentRepository.save(a);
        auditService.record("LABOUR_ASSIGNED", "LabourAssignment", saved.getId(),
                "labourId=" + labourId + "; projectId=" + projectId + "; dailyRate=" + saved.getDailyRate());
        return LabourAssignmentResponse.from(saved);
    }

    @Override
    public LabourAssignmentResponse updateAssignment(int assignmentId, LabourAssignmentUpdateRequest request) {
        LabourAssignment a = guard.assignmentForContractor(assignmentId);
        a.setDailyRate(Money.money(request.getDailyRate()));
        a.setAdvanceTaken(Money.money(request.getAdvanceTaken()));
        LabourAssignment saved = assignmentRepository.save(a);
        auditService.record("ASSIGNMENT_UPDATED", "LabourAssignment", assignmentId,
                "dailyRate=" + saved.getDailyRate() + "; advanceTaken=" + saved.getAdvanceTaken());
        return LabourAssignmentResponse.from(saved);
    }

    @Override
    public LabourAssignmentResponse updateStatus(int assignmentId, AssignmentStatus status) {
        LabourAssignment a = guard.assignmentForContractor(assignmentId);
        a.setStatus(status);
        LabourAssignment saved = assignmentRepository.save(a);
        auditService.record("ASSIGNMENT_STATUS_CHANGED", "LabourAssignment", assignmentId, "status=" + status);
        return LabourAssignmentResponse.from(saved);
    }

    @Override
    public List<LabourAssignmentResponse> getAssignmentsByProject(int projectId, Pageable pageable) {
        guard.projectForOwnerOrContractor(projectId);
        return assignmentRepository.findByProjectId(projectId, pageable)
                .map(LabourAssignmentResponse::from)
                .getContent();
    }

    /** Assignments relevant to the logged-in user, depending on their role. */
    @Override
    public List<LabourAssignmentResponse> getMyAssignments(Pageable pageable) {
        int id = currentUser.id();
        Page<LabourAssignment> page = switch (currentUser.role()) {
            case LABOUR -> assignmentRepository.findByLabourId(id, pageable);
            case CONTRACTOR -> assignmentRepository.findByProjectContractorId(id, pageable);
            case HOUSE_OWNER -> assignmentRepository.findByProjectHouseOwnerId(id, pageable);
            case ENGINEER -> Page.empty(pageable);
            case ADMIN -> assignmentRepository.findAll(pageable);
        };
        return page.map(LabourAssignmentResponse::from).getContent();
    }
}
