package com.construction.construction_management.service.impl;

import com.construction.construction_management.dto.ProjectRequirementRequest;
import com.construction.construction_management.dto.ProjectRequirementResponse;
import com.construction.construction_management.dto.UserResponse;
import com.construction.construction_management.entity.ProjectRequirement;
import com.construction.construction_management.entity.RequirementStatus;
import com.construction.construction_management.exception.ResourceNotFoundException;
import com.construction.construction_management.repository.ProjectRequirementRepository;
import com.construction.construction_management.security.CurrentUser;
import com.construction.construction_management.service.ProjectRequirementService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ProjectRequirementServiceImpl implements ProjectRequirementService {

    private final ProjectRequirementRepository requirementRepository;
    private final CurrentUser currentUser;

    public ProjectRequirementServiceImpl(ProjectRequirementRepository requirementRepository,
                                         CurrentUser currentUser) {
        this.requirementRepository = requirementRepository;
        this.currentUser = currentUser;
    }

    private ProjectRequirementResponse toResponse(ProjectRequirement r) {
        return new ProjectRequirementResponse(
                r.getId(),
                UserResponse.from(r.getHouseOwner()),
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
        r.setAreaSqFt(request.getAreaSqFt());
        r.setContractType(request.getContractType());
        r.setDescription(request.getDescription());
        r.setStatus(RequirementStatus.OPEN);
        r.setPostedDate(LocalDate.now());
        return toResponse(requirementRepository.save(r));
    }

    /** Open requirements for contractors to browse, optionally filtered by city. */
    @Override
    public List<ProjectRequirementResponse> getOpenRequirements(String city) {
        List<ProjectRequirement> list = (city == null || city.isBlank())
                ? requirementRepository.findByStatus(RequirementStatus.OPEN)
                : requirementRepository.findByCityIgnoreCaseAndStatus(city.trim(), RequirementStatus.OPEN);
        return list.stream().map(this::toResponse).toList();
    }

    @Override
    public List<ProjectRequirementResponse> getMyRequirements() {
        return requirementRepository.findByHouseOwnerId(currentUser.id()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public ProjectRequirementResponse closeRequirement(int id) {
        ProjectRequirement r = requirementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Requirement not found"));
        if (r.getHouseOwner().getId() != currentUser.id()) {
            throw new AccessDeniedException("Only the owner can close this requirement");
        }
        r.setStatus(RequirementStatus.CLOSED);
        return toResponse(requirementRepository.save(r));
    }
}
