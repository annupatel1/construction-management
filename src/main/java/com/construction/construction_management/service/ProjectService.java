package com.construction.construction_management.service;

import com.construction.construction_management.dto.ProjectRequest;
import com.construction.construction_management.dto.ProjectResponse;
import com.construction.construction_management.entity.ProjectStatus;

import java.util.List;

public interface ProjectService {
    ProjectResponse createProject(int contractorId, ProjectRequest request);
    List<ProjectResponse> getMyProjects();
    ProjectResponse getProjectById(int id);
    ProjectResponse updateStatus(int id, ProjectStatus status);
}
