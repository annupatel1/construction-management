package com.construction.construction_management.service.impl;

import com.construction.construction_management.dto.DirectoryUserResponse;
import com.construction.construction_management.dto.ProjectRequirementRequest;
import com.construction.construction_management.dto.ProjectRequirementResponse;
import com.construction.construction_management.entity.ProjectRequirement;
import com.construction.construction_management.entity.RequirementStatus;
import com.construction.construction_management.exception.ResourceNotFoundException;
import com.construction.construction_management.repository.ProjectRequirementRepository;
import com.construction.construction_management.security.CurrentUser;
import com.construction.construction_management.service.AuditService;
import com.construction.construction_management.service.ProjectRequirementService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.construction.construction_management.util.Money;

import java.time.LocalDate;
import java.util.List;

@Service
public class ProjectRequirementServiceImpl implements ProjectRequirementService {

    private final ProjectRequirementRepository requirementRepository;
    private final CurrentUser currentUser;
    private final AuditService auditService;

    public ProjectRequirementServiceImpl(ProjectRequirementRepository requirementRepository,
                                         CurrentUser currentUser,
                                         AuditService auditService) {
        this.requirementRepository = requirementRepository;
        this.currentUser = currentUser;
        this.auditService = auditService;
    }

    private ProjectRequirementResponse toResponse(ProjectRequirement r) {
        return new ProjectRequirementResponse(
                r.getId(),
                DirectoryUserResponse.from(r.getHouseOwner()),
                r.getCity(),
                r.getArea(),
                r.getPincode(),
                r.getAreaSqFt(),
                r.getContractType().toString(),
                r.getDescription(),
                r.getStatus().toString(),
                r.getPostedDate()
        );
    }

    @Override
    public ProjectRequirementResponse postRequirement(ProjectRequirementRequest request) {
        ProjectRequirement r = new ProjectRequirement();
        r.setHouseOwner(currentUser.get());
        r.setCity(request.getCity().trim());
        r.setArea(request.getArea().trim());
        r.setPincode(request.getPincode());
        r.setAreaSqFt(Money.money(request.getAreaSqFt()));
        r.setContractType(request.getContractType());
        r.setDescription(request.getDescription());
        r.setStatus(RequirementStatus.OPEN);
        r.setPostedDate(LocalDate.now());
        ProjectRequirement saved = requirementRepository.save(r);
        auditService.record("REQUIREMENT_POSTED", "ProjectRequirement", saved.getId(),
                "city=" + saved.getCity() + "; areaSqFt=" + saved.getAreaSqFt());
        return toResponse(saved);
    }

    /** Open requirements for contractors to browse, optionally filtered by city. */
    @Override
    public List<ProjectRequirementResponse> getOpenRequirements(String city, Pageable pageable) {
        Page<ProjectRequirement> page = (city == null || city.isBlank())
                ? requirementRepository.findByStatus(RequirementStatus.OPEN, pageable)
                : requirementRepository.findByCityIgnoreCaseAndStatus(city.trim(), RequirementStatus.OPEN, pageable);
        return page.map(this::toResponse).getContent();
    }

    @Override
    public List<ProjectRequirementResponse> getMyRequirements(Pageable pageable) {
        return requirementRepository.findByHouseOwnerId(currentUser.id(), pageable)
                .map(this::toResponse)
                .getContent();
    }

    @Override
    public ProjectRequirementResponse closeRequirement(int id) {
        ProjectRequirement r = requirementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Requirement not found"));
        if (r.getHouseOwner().getId() != currentUser.id()) {
            throw new AccessDeniedException("Only the owner can close this requirement");
        }
        r.setStatus(RequirementStatus.CLOSED);
        ProjectRequirement saved = requirementRepository.save(r);
        auditService.record("REQUIREMENT_CLOSED", "ProjectRequirement", id, null);
        return toResponse(saved);
    }
}
