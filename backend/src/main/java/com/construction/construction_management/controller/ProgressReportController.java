package com.construction.construction_management.controller;

import com.construction.construction_management.dto.ProgressReportRequest;
import com.construction.construction_management.dto.ProgressReportResponse;
import com.construction.construction_management.service.ProgressReportService;
import com.construction.construction_management.util.Pages;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/progress-reports")
public class ProgressReportController {

    private final ProgressReportService progressReportService;

    public ProgressReportController(ProgressReportService progressReportService) {
        this.progressReportService = progressReportService;
    }

    @GetMapping
    public List<ProgressReportResponse> list(@PathVariable int projectId,
                                             @RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "50") int size) {
        return progressReportService.list(projectId, Pages.of(page, size));
    }

    @GetMapping("/{reportId}")
    public ProgressReportResponse get(@PathVariable int projectId, @PathVariable int reportId) {
        return progressReportService.get(projectId, reportId);
    }

    @PostMapping
    public ProgressReportResponse create(@PathVariable int projectId,
                                         @Valid @RequestBody ProgressReportRequest request) {
        return progressReportService.create(projectId, request);
    }

    @PutMapping("/{reportId}")
    public ProgressReportResponse update(@PathVariable int projectId, @PathVariable int reportId,
                                         @Valid @RequestBody ProgressReportRequest request) {
        return progressReportService.update(projectId, reportId, request);
    }

    @PostMapping("/{reportId}/submit")
    public ProgressReportResponse submit(@PathVariable int projectId, @PathVariable int reportId) {
        return progressReportService.submit(projectId, reportId);
    }

    @PostMapping("/{reportId}/publish")
    public ProgressReportResponse publish(@PathVariable int projectId, @PathVariable int reportId) {
        return progressReportService.publish(projectId, reportId);
    }

    @DeleteMapping("/{reportId}")
    public void delete(@PathVariable int projectId, @PathVariable int reportId) {
        progressReportService.delete(projectId, reportId);
    }
}
