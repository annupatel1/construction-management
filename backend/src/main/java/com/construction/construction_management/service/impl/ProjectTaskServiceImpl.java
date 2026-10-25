package com.construction.construction_management.service.impl;

import com.construction.construction_management.dto.TaskRequest;
import com.construction.construction_management.dto.TaskResponse;
import com.construction.construction_management.dto.TaskStatusRequest;
import com.construction.construction_management.entity.Milestone;
import com.construction.construction_management.entity.Project;
import com.construction.construction_management.entity.ProjectTask;
import com.construction.construction_management.entity.RoleName;
import com.construction.construction_management.entity.TaskStatus;
import com.construction.construction_management.entity.User;
import com.construction.construction_management.exception.BadRequestException;
import com.construction.construction_management.exception.ResourceNotFoundException;
import com.construction.construction_management.repository.MilestoneRepository;
import com.construction.construction_management.repository.ProjectTaskRepository;
import com.construction.construction_management.repository.UserRepository;
import com.construction.construction_management.security.CurrentUser;
import com.construction.construction_management.service.AccessGuard;
import com.construction.construction_management.service.AuditService;
import com.construction.construction_management.service.ProjectTaskService;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProjectTaskServiceImpl implements ProjectTaskService {

    private final ProjectTaskRepository taskRepository;
    private final MilestoneRepository milestoneRepository;
    private final UserRepository userRepository;
    private final CurrentUser currentUser;
    private final AccessGuard guard;
    private final AuditService auditService;

    public ProjectTaskServiceImpl(ProjectTaskRepository taskRepository,
                                  MilestoneRepository milestoneRepository,
                                  UserRepository userRepository,
                                  CurrentUser currentUser,
                                  AccessGuard guard,
                                  AuditService auditService) {
        this.taskRepository = taskRepository;
        this.milestoneRepository = milestoneRepository;
        this.userRepository = userRepository;
        this.currentUser = currentUser;
        this.guard = guard;
        this.auditService = auditService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponse> list(int projectId, String status, Integer assigneeId, Pageable pageable) {
        guard.projectForSiteRead(projectId);
        TaskStatus filter = null;
        if (status != null && !status.isBlank()) {
            try {
                filter = TaskStatus.valueOf(status.trim().toUpperCase());
            } catch (IllegalArgumentException ex) {
                throw new BadRequestException("Invalid task status");
            }
        }
        TaskStatus finalFilter = filter;
        Specification<ProjectTask> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("project").get("id"), projectId));
            if (finalFilter != null) {
                predicates.add(cb.equal(root.get("status"), finalFilter));
            }
            if (assigneeId != null) {
                predicates.add(cb.equal(root.get("assignee").get("id"), assigneeId));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
        Pageable page = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                Sort.by(Sort.Direction.ASC, "dueDate").and(Sort.by(Sort.Direction.DESC, "id")));
        return taskRepository.findAll(spec, page).map(TaskResponse::from).getContent();
    }

    @Override
    @Transactional
    public TaskResponse create(int projectId, TaskRequest request) {
        Project project = guard.projectForSiteManagement(projectId);
        ProjectTask task = new ProjectTask();
        task.setProject(project);
        task.setCreatedBy(currentUser.get());
        task.setStatus(TaskStatus.TODO);
        apply(project, task, request);
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        ProjectTask saved = taskRepository.save(task);
        auditService.record("TASK_CREATED", "ProjectTask", saved.getId(),
                "projectId=" + projectId + "; title=" + saved.getTitle());
        return TaskResponse.from(saved);
    }

    @Override
    @Transactional
    public TaskResponse update(int projectId, int taskId, TaskRequest request) {
        Project project = guard.projectForSiteManagement(projectId);
        ProjectTask task = load(projectId, taskId);
        apply(project, task, request);
        task.setUpdatedAt(LocalDateTime.now());
        ProjectTask saved = taskRepository.save(task);
        auditService.record("TASK_UPDATED", "ProjectTask", saved.getId(),
                "projectId=" + projectId + "; status=" + saved.getStatus());
        return TaskResponse.from(saved);
    }

    @Override
    @Transactional
    public TaskResponse updateStatus(int projectId, int taskId, TaskStatusRequest request) {
        guard.projectForSiteRead(projectId);
        ProjectTask task = load(projectId, taskId);
        RoleName role = currentUser.role();
        boolean manager = role == RoleName.CONTRACTOR || role == RoleName.ADMIN;
        boolean assignee = task.getAssignee() != null && task.getAssignee().getId() == currentUser.id();
        if (!manager && !assignee) {
            throw new AccessDeniedException("Only the contractor or the assignee can change this task");
        }
        if (request.getStatus() == TaskStatus.BLOCKED
                && (request.getDelayReason() == null || request.getDelayReason().isBlank())) {
            throw new BadRequestException("Add a delay reason when a task is blocked");
        }

        task.setStatus(request.getStatus());
        task.setDelayReason(request.getStatus() == TaskStatus.BLOCKED
                ? clean(request.getDelayReason()) : null);
        task.setCompletedAt(request.getStatus() == TaskStatus.DONE ? LocalDateTime.now() : null);
        task.setUpdatedAt(LocalDateTime.now());
        ProjectTask saved = taskRepository.save(task);
        auditService.record("TASK_STATUS_CHANGED", "ProjectTask", saved.getId(),
                "projectId=" + projectId + "; status=" + saved.getStatus());
        return TaskResponse.from(saved);
    }

    @Override
    @Transactional
    public void delete(int projectId, int taskId) {
        guard.projectForSiteManagement(projectId);
        ProjectTask task = load(projectId, taskId);
        taskRepository.delete(task);
        auditService.record("TASK_DELETED", "ProjectTask", taskId, "projectId=" + projectId);
    }

    private ProjectTask load(int projectId, int taskId) {
        return taskRepository.findByIdAndProjectId(taskId, projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
    }

    private void apply(Project project, ProjectTask task, TaskRequest request) {
        Milestone milestone = null;
        if (request.getMilestoneId() != null) {
            milestone = milestoneRepository.findByIdAndProjectId(request.getMilestoneId(), project.getId())
                    .orElseThrow(() -> new BadRequestException("Milestone does not belong to this project"));
        }
        User assignee = null;
        if (request.getAssigneeId() != null) {
            assignee = userRepository.findById(request.getAssigneeId())
                    .orElseThrow(() -> new BadRequestException("Assignee not found"));
            if (!guard.isAssignable(project, assignee.getId())) {
                throw new BadRequestException("Assignee is not part of this project");
            }
        }
        task.setTitle(request.getTitle().trim());
        task.setDescription(clean(request.getDescription()));
        task.setMilestone(milestone);
        task.setAssignee(assignee);
        task.setDueDate(request.getDueDate());
        if (request.getDelayReason() != null && !request.getDelayReason().isBlank()) {
            task.setDelayReason(request.getDelayReason().trim());
        }
    }

    private static String clean(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
