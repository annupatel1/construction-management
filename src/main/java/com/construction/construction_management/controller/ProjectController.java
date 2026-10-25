package com.construction.construction_management.controller;

import com.construction.construction_management.dto.ProjectRequest;
import com.construction.construction_management.dto.ProjectResponse;
import com.construction.construction_management.dto.ProjectStatusRequest;
import com.construction.construction_management.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    /** The house owner is taken from the login token, not from the URL. */
    @PreAuthorize("hasRole('HOUSE_OWNER')")
    @PostMapping("/{contractorId}")
    public ProjectResponse createProject(@PathVariable int contractorId,
                                         @Valid @RequestBody ProjectRequest request) {
        return projectService.createProject(contractorId, request);
    }

    /** Projects belonging to the logged-in user. */
    @GetMapping
    public List<ProjectResponse> getMyProjects() {
        return projectService.getMyProjects();
    }

    @GetMapping("/{id}")
    public ProjectResponse getProjectById(@PathVariable int id) {
        return projectService.getProjectById(id);
    }

    @PreAuthorize("hasAnyRole('HOUSE_OWNER','CONTRACTOR')")
    @PatchMapping("/{id}/status")
    public ProjectResponse updateStatus(@PathVariable int id,
                                        @Valid @RequestBody ProjectStatusRequest request) {
        return projectService.updateStatus(id, request.getStatus());
    }
}
