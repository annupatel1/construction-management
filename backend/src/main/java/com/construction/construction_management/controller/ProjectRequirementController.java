package com.construction.construction_management.controller;

import com.construction.construction_management.dto.ProjectRequirementRequest;
import com.construction.construction_management.dto.ProjectRequirementResponse;
import com.construction.construction_management.service.ProjectRequirementService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/requirements")
public class ProjectRequirementController {

    private final ProjectRequirementService requirementService;

    public ProjectRequirementController(ProjectRequirementService requirementService) {
        this.requirementService = requirementService;
    }

    @PreAuthorize("hasRole('HOUSE_OWNER')")
    @PostMapping
    public ProjectRequirementResponse postRequirement(@Valid @RequestBody ProjectRequirementRequest request) {
        return requirementService.postRequirement(request);
    }

    /** Contractors browse open requirements, optionally filtered by ?city=Indore */
    @PreAuthorize("hasAnyRole('CONTRACTOR','ADMIN')")
    @GetMapping
    public List<ProjectRequirementResponse> getOpenRequirements(@RequestParam(required = false) String city) {
        return requirementService.getOpenRequirements(city);
    }

    @PreAuthorize("hasRole('HOUSE_OWNER')")
    @GetMapping("/my")
    public List<ProjectRequirementResponse> getMyRequirements() {
        return requirementService.getMyRequirements();
    }

    @PreAuthorize("hasRole('HOUSE_OWNER')")
    @PatchMapping("/{id}/close")
    public ProjectRequirementResponse closeRequirement(@PathVariable int id) {
        return requirementService.closeRequirement(id);
    }
}
