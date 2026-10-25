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
import com.construction.construction_management.service.AuditService;
import com.construction.construction_management.service.AccessGuard;
import com.construction.construction_management.service.ProjectService;
import com.construction.construction_management.util.Money;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final LabourAssignmentRepository assignmentRepository;
    private final CurrentUser currentUser;
    private final AccessGuard guard;
    private final AuditService auditService;

    public ProjectServiceImpl(ProjectRepository projectRepository,
                              UserRepository userRepository,
                              LabourAssignmentRepository assignmentRepository,
                              CurrentUser currentUser,
                              AccessGuard guard,
                              AuditService auditService) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.assignmentRepository = assignmentRepository;
        this.currentUser = currentUser;
        this.guard = guard;
        this.auditService = auditService;
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
        BigDecimal area = Money.money(request.getAreaSqFt());
        BigDecimal rate = Money.money(request.getRatePerSqFt());
        project.setAreaSqFt(area);
        project.setRatePerSqFt(rate);
        project.setTotalEstimatedCost(Money.multiply(area, rate));
        project.setContractType(request.getContractType());
        project.setStatus(ProjectStatus.ONGOING);
        project.setStartDate(request.getStartDate());
        project.setEndDate(request.getEndDate());

        Project saved = projectRepository.save(project);
        auditService.record("PROJECT_CREATED", "Project", saved.getId(),
                "title=" + saved.getTitle() + "; contractType=" + saved.getContractType());
        return ProjectResponse.from(saved);
    }

    @Override
    public ProjectResponse createOwnProject(ProjectRequest request) {
        Project project = buildProject(request);
        project.setHouseOwner(null);
        project.setContractor(currentUser.get());
        Project saved = projectRepository.save(project);
        auditService.record("PROJECT_CREATED", "Project", saved.getId(),
                "title=" + saved.getTitle() + "; contractType=" + saved.getContractType());
        return ProjectResponse.from(saved);
    }

    private Project buildProject(ProjectRequest request) {
        if (request.getEndDate() != null && request.getEndDate().isBefore(request.getStartDate())) {
            throw new BadRequestException("End date cannot be before start date");
        }

        Project project = new Project();
        project.setTitle(request.getTitle().trim());
        project.setLocation(request.getLocation().trim());
        BigDecimal area = Money.money(request.getAreaSqFt());
        BigDecimal rate = Money.money(request.getRatePerSqFt());
        project.setAreaSqFt(area);
        project.setRatePerSqFt(rate);
        project.setTotalEstimatedCost(Money.multiply(area, rate));
        project.setContractType(request.getContractType());
        project.setStatus(ProjectStatus.ONGOING);
        project.setStartDate(request.getStartDate());
        project.setEndDate(request.getEndDate());
        return project;
    }

    /** Projects that belong to the logged-in user, depending on their role. */
    @Override
    public List<ProjectResponse> getMyProjects(Pageable pageable) {
        int id = currentUser.id();
        Page<Project> projects = switch (currentUser.role()) {
            case HOUSE_OWNER -> projectRepository.findByHouseOwnerId(id, pageable);
            case CONTRACTOR -> projectRepository.findByContractorId(id, pageable);
            case LABOUR -> assignmentRepository.findDistinctProjectsByLabourId(id, pageable);
            case ADMIN -> projectRepository.findAll(pageable);
        };
        return projects.map(ProjectResponse::from).getContent();
    }

    @Override
    public ProjectResponse getProjectById(int id) {
        return ProjectResponse.from(guard.projectForParticipant(id));
    }

    @Override
    public ProjectResponse updateStatus(int id, ProjectStatus status) {
        Project project = guard.projectForOwnerOrContractor(id);
        project.setStatus(status);
        Project saved = projectRepository.save(project);
        auditService.record("PROJECT_STATUS_CHANGED", "Project", id, "status=" + status);
        return ProjectResponse.from(saved);
    }
}
