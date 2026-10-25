package com.construction.construction_management.service;

import com.construction.construction_management.dto.ProjectRequirementRequest;
import com.construction.construction_management.dto.ProjectRequirementResponse;

import java.util.List;

public interface ProjectRequirementService {
    ProjectRequirementResponse postRequirement(ProjectRequirementRequest request);
    List<ProjectRequirementResponse> getOpenRequirements(String city);
    List<ProjectRequirementResponse> getMyRequirements();
    ProjectRequirementResponse closeRequirement(int id);
}
