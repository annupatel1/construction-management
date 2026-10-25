package com.construction.construction_management.service;

import org.springframework.data.domain.Pageable;

import com.construction.construction_management.dto.ProjectRequirementRequest;
import com.construction.construction_management.dto.ProjectRequirementResponse;

import java.util.List;

public interface ProjectRequirementService {
    ProjectRequirementResponse postRequirement(ProjectRequirementRequest request);
    List<ProjectRequirementResponse> getOpenRequirements(String city, Pageable pageable);
    List<ProjectRequirementResponse> getMyRequirements(Pageable pageable);
    ProjectRequirementResponse closeRequirement(int id);
}
