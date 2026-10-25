package com.construction.construction_management.controller;

import com.construction.construction_management.dto.TaskRequest;
import com.construction.construction_management.dto.TaskResponse;
import com.construction.construction_management.dto.TaskStatusRequest;
import com.construction.construction_management.service.ProjectTaskService;
import com.construction.construction_management.util.Pages;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/tasks")
public class ProjectTaskController {

    private final ProjectTaskService taskService;

    public ProjectTaskController(ProjectTaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public List<TaskResponse> list(@PathVariable int projectId,
                                   @RequestParam(required = false) String status,
                                   @RequestParam(required = false) Integer assigneeId,
                                   @RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "100") int size) {
        return taskService.list(projectId, status, assigneeId, Pages.of(page, size));
    }

    @PostMapping
    public TaskResponse create(@PathVariable int projectId,
                               @Valid @RequestBody TaskRequest request) {
        return taskService.create(projectId, request);
    }

    @PutMapping("/{taskId}")
    public TaskResponse update(@PathVariable int projectId, @PathVariable int taskId,
                               @Valid @RequestBody TaskRequest request) {
        return taskService.update(projectId, taskId, request);
    }

    @PatchMapping("/{taskId}/status")
    public TaskResponse updateStatus(@PathVariable int projectId, @PathVariable int taskId,
                                     @Valid @RequestBody TaskStatusRequest request) {
        return taskService.updateStatus(projectId, taskId, request);
    }

    @DeleteMapping("/{taskId}")
    public void delete(@PathVariable int projectId, @PathVariable int taskId) {
        taskService.delete(projectId, taskId);
    }
}
