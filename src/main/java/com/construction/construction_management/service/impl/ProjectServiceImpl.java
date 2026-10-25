package com.construction.construction_management.service.impl;

import com.construction.construction_management.dto.ProjectRequest;
import com.construction.construction_management.dto.ProjectResponse;
import com.construction.construction_management.entity.*;
import com.construction.construction_management.exception.BadRequestException;
import com.construction.construction_management.exception.ResourceNotFoundException;
import com.construction.construction_management.repository.LabourAssignmentRepository;
import com.construction.construction_management.repository.ProjectRepository;
import com.construction.construction_management.repository.UserRepository;
import com.construction.construction_management.security.CurrentUser;
import com.construction.construction_management.service.AccessGuard;
import com.construction.construction_management.service.ProjectService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final LabourAssignmentRepository assignmentRepository;
    private final CurrentUser currentUser;
    private final AccessGuard guard;

    public ProjectServiceImpl(ProjectRepository projectRepository,
                              UserRepository userRepository,
                              LabourAssignmentRepository assignmentRepository,
                              CurrentUser currentUser,
                              AccessGuard guard) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.assignmentRepository = assignmentRepository;
        this.currentUser = currentUser;
        this.guard = guard;
    }

    @Override
    public ProjectResponse createProject(int contractorId, ProjectRequest request) {
        User owner = currentUser.get();

        User contractor = userRepository.findById(contractorId)
                .orElseThrow(() -> new ResourceNotFoundException("Contractor not found"));
        if (contractor.getRole().getName() != RoleName.CONTRACTOR) {
            throw new BadRequestException("Selected user is not a contractor");
        }
        if (request.getEndDate() != null && request.getEndDate().isBefore(request.getStartDate())) {
            throw new BadRequestException("End date cannot be before start date");
        }

        Project project = new Project();
        project.setHouseOwner(owner);
        project.setContractor(contractor);
        project.setTitle(request.getTitle().trim());
        project.setLocation(request.getLocation().trim());
        project.setAreaSqFt(request.getAreaSqFt());
        project.setRatePerSqFt(request.getRatePerSqFt());
        project.setTotalEstimatedCost(request.getAreaSqFt() * request.getRatePerSqFt());
        project.setContractType(request.getContractType());
        project.setStatus(ProjectStatus.ONGOING);
        project.setStartDate(request.getStartDate());
        project.setEndDate(request.getEndDate());

        return ProjectResponse.from(projectRepository.save(project));
    }

    /** Projects that belong to the logged-in user, depending on their role. */
    @Override
    public List<ProjectResponse> getMyProjects() {
        int id = currentUser.id();
        List<Project> projects = switch (currentUser.role()) {
            case HOUSE_OWNER -> projectRepository.findByHouseOwnerId(id);
            case CONTRACTOR -> projectRepository.findByContractorId(id);
            case LABOUR -> assignmentRepository.findByLabourId(id).stream()
                    .map(LabourAssignment::getProject)
                    .distinct()
                    .toList();
            case ADMIN -> projectRepository.findAll();
        };
        return projects.stream().map(ProjectResponse::from).toList();
    }

    @Override
    public ProjectResponse getProjectById(int id) {
        return ProjectResponse.from(guard.projectForParticipant(id));
    }

    @Override
    public ProjectResponse updateStatus(int id, ProjectStatus status) {
        Project project = guard.projectForOwnerOrContractor(id);
        project.setStatus(status);
        return ProjectResponse.from(projectRepository.save(project));
    }
}
