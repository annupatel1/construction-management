package com.construction.construction_management.controller;

import com.construction.construction_management.dto.IssueCommentRequest;
import com.construction.construction_management.dto.IssueCommentResponse;
import com.construction.construction_management.dto.IssueRequest;
import com.construction.construction_management.dto.IssueResponse;
import com.construction.construction_management.dto.IssueStatusRequest;
import com.construction.construction_management.service.SiteIssueService;
import com.construction.construction_management.util.Pages;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/issues")
public class SiteIssueController {

    private final SiteIssueService issueService;

    public SiteIssueController(SiteIssueService issueService) {
        this.issueService = issueService;
    }

    @GetMapping
    public List<IssueResponse> list(@PathVariable int projectId,
                                    @RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "100") int size) {
        return issueService.list(projectId, Pages.of(page, size));
    }

    @GetMapping("/{issueId}")
    public IssueResponse get(@PathVariable int projectId, @PathVariable int issueId) {
        return issueService.get(projectId, issueId);
    }

    @PostMapping
    public IssueResponse create(@PathVariable int projectId,
                                @Valid @RequestBody IssueRequest request) {
        return issueService.create(projectId, request);
    }

    @PutMapping("/{issueId}")
    public IssueResponse update(@PathVariable int projectId, @PathVariable int issueId,
                                @Valid @RequestBody IssueRequest request) {
        return issueService.update(projectId, issueId, request);
    }

    @PatchMapping("/{issueId}/status")
    public IssueResponse updateStatus(@PathVariable int projectId, @PathVariable int issueId,
                                      @Valid @RequestBody IssueStatusRequest request) {
        return issueService.updateStatus(projectId, issueId, request);
    }

    @PostMapping("/{issueId}/comments")
    public IssueCommentResponse comment(@PathVariable int projectId, @PathVariable int issueId,
                                        @Valid @RequestBody IssueCommentRequest request) {
        return issueService.addComment(projectId, issueId, request);
    }

    @DeleteMapping("/{issueId}")
    public void delete(@PathVariable int projectId, @PathVariable int issueId) {
        issueService.delete(projectId, issueId);
    }
}
