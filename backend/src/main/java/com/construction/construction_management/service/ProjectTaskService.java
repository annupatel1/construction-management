package com.construction.construction_management.service;

import com.construction.construction_management.dto.TaskRequest;
import com.construction.construction_management.dto.TaskResponse;
import com.construction.construction_management.dto.TaskStatusRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProjectTaskService {
    List<TaskResponse> list(int projectId, String status, Integer assigneeId, Pageable pageable);

    TaskResponse create(int projectId, TaskRequest request);

    TaskResponse update(int projectId, int taskId, TaskRequest request);

    TaskResponse updateStatus(int projectId, int taskId, TaskStatusRequest request);

    void delete(int projectId, int taskId);
}
