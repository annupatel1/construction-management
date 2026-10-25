package com.construction.construction_management.controller;

import com.construction.construction_management.dto.MilestoneRequest;
import com.construction.construction_management.dto.MilestoneResponse;
import com.construction.construction_management.service.MilestoneService;
import com.construction.construction_management.util.Pages;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/milestones")
public class MilestoneController {

    private final MilestoneService milestoneService;

    public MilestoneController(MilestoneService milestoneService) {
        this.milestoneService = milestoneService;
    }

    @GetMapping
    public List<MilestoneResponse> list(@PathVariable int projectId,
                                        @RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "100") int size) {
        return milestoneService.list(projectId, Pages.of(page, size));
    }

    @PostMapping
    public MilestoneResponse create(@PathVariable int projectId,
                                    @Valid @RequestBody MilestoneRequest request) {
        return milestoneService.create(projectId, request);
    }

    @PutMapping("/{milestoneId}")
    public MilestoneResponse update(@PathVariable int projectId, @PathVariable int milestoneId,
                                    @Valid @RequestBody MilestoneRequest request) {
        return milestoneService.update(projectId, milestoneId, request);
    }

    @DeleteMapping("/{milestoneId}")
    public void delete(@PathVariable int projectId, @PathVariable int milestoneId) {
        milestoneService.delete(projectId, milestoneId);
    }
}
