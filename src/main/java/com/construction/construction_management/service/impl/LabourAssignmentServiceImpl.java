package com.construction.construction_management.service.impl;

import com.construction.construction_management.dto.LabourAssignmentRequest;
import com.construction.construction_management.dto.LabourAssignmentResponse;
import com.construction.construction_management.entity.*;
import com.construction.construction_management.exception.BadRequestException;
import com.construction.construction_management.exception.ConflictException;
import com.construction.construction_management.exception.ResourceNotFoundException;
import com.construction.construction_management.repository.LabourAssignmentRepository;
import com.construction.construction_management.repository.UserRepository;
import com.construction.construction_management.security.CurrentUser;
import com.construction.construction_management.service.AccessGuard;
import com.construction.construction_management.service.LabourAssignmentService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class LabourAssignmentServiceImpl implements LabourAssignmentService {

    private final LabourAssignmentRepository assignmentRepository;
    private final UserRepository userRepository;
    private final CurrentUser currentUser;
    private final AccessGuard guard;

    public LabourAssignmentServiceImpl(LabourAssignmentRepository assignmentRepository,
                                       UserRepository userRepository,
                                       CurrentUser currentUser,
                                       AccessGuard guard) {
        this.assignmentRepository = assignmentRepository;
        this.userRepository = userRepository;
        this.currentUser = currentUser;
        this.guard = guard;
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
        a.setDailyRate(request.getDailyRate());
        a.setStatus(AssignmentStatus.ASSIGNED);
        a.setAdvanceTaken(request.getAdvanceTaken());
        a.setAssignedDate(request.getAssignedDate() != null ? request.getAssignedDate() : LocalDate.now());

        return LabourAssignmentResponse.from(assignmentRepository.save(a));
    }

    @Override
    public List<LabourAssignmentResponse> getAssignmentsByProject(int projectId) {
        guard.projectForOwnerOrContractor(projectId);
        return assignmentRepository.findByProjectId(projectId).stream()
                .map(LabourAssignmentResponse::from)
                .toList();
    }

    /** Assignments relevant to the logged-in user, depending on their role. */
    @Override
    public List<LabourAssignmentResponse> getMyAssignments() {
        int id = currentUser.id();
        List<LabourAssignment> list = switch (currentUser.role()) {
            case LABOUR -> assignmentRepository.findByLabourId(id);
            case CONTRACTOR -> assignmentRepository.findByProjectContractorId(id);
            case HOUSE_OWNER -> assignmentRepository.findByProjectHouseOwnerId(id);
            case ADMIN -> assignmentRepository.findAll();
        };
        return list.stream().map(LabourAssignmentResponse::from).toList();
    }
}
