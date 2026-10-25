package com.construction.construction_management.service.impl;

import com.construction.construction_management.dto.MilestoneRequest;
import com.construction.construction_management.dto.MilestoneResponse;
import com.construction.construction_management.entity.Milestone;
import com.construction.construction_management.entity.MilestoneStatus;
import com.construction.construction_management.entity.Project;
import com.construction.construction_management.entity.ProjectTask;
import com.construction.construction_management.entity.TaskStatus;
import com.construction.construction_management.exception.BadRequestException;
import com.construction.construction_management.exception.ResourceNotFoundException;
import com.construction.construction_management.repository.MilestoneRepository;
import com.construction.construction_management.repository.ProjectTaskRepository;
import com.construction.construction_management.service.AccessGuard;
import com.construction.construction_management.service.AuditService;
import com.construction.construction_management.service.MilestoneService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class MilestoneServiceImpl implements MilestoneService {

    private final MilestoneRepository milestoneRepository;
    private final ProjectTaskRepository taskRepository;
    private final AccessGuard guard;
    private final AuditService auditService;

    public MilestoneServiceImpl(MilestoneRepository milestoneRepository,
                                ProjectTaskRepository taskRepository,
                                AccessGuard guard,
                                AuditService auditService) {
        this.milestoneRepository = milestoneRepository;
        this.taskRepository = taskRepository;
        this.guard = guard;
        this.auditService = auditService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<MilestoneResponse> list(int projectId, Pageable pageable) {
        guard.projectForSiteRead(projectId);
        Pageable page = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
        return milestoneRepository.findByProjectIdOrderByTargetDateAscIdAsc(projectId, page).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public MilestoneResponse create(int projectId, MilestoneRequest request) {
        Project project = guard.projectForSiteManagement(projectId);
        Milestone milestone = new Milestone();
        milestone.setProject(project);
        apply(milestone, request);
        milestone.setCreatedAt(LocalDateTime.now());
        milestone.setUpdatedAt(LocalDateTime.now());
        Milestone saved = milestoneRepository.save(milestone);
        auditService.record("MILESTONE_CREATED", "Milestone", saved.getId(),
                "projectId=" + projectId + "; title=" + saved.getTitle());
        return toResponse(saved);
    }

    @Override
    @Transactional
    public MilestoneResponse update(int projectId, int milestoneId, MilestoneRequest request) {
        guard.projectForSiteManagement(projectId);
        Milestone milestone = load(projectId, milestoneId);
        apply(milestone, request);
        milestone.setUpdatedAt(LocalDateTime.now());
        Milestone saved = milestoneRepository.save(milestone);
        auditService.record("MILESTONE_UPDATED", "Milestone", saved.getId(),
                "projectId=" + projectId + "; status=" + saved.getStatus());
        return toResponse(saved);
    }

    @Override
    @Transactional
    public void delete(int projectId, int milestoneId) {
        guard.projectForSiteManagement(projectId);
        Milestone milestone = load(projectId, milestoneId);
        List<ProjectTask> tasks = taskRepository.findByMilestoneId(milestoneId);
        tasks.forEach(task -> task.setMilestone(null));
        taskRepository.saveAll(tasks);
        milestoneRepository.delete(milestone);
        auditService.record("MILESTONE_DELETED", "Milestone", milestoneId,
                "projectId=" + projectId);
    }

    private Milestone load(int projectId, int milestoneId) {
        return milestoneRepository.findByIdAndProjectId(milestoneId, projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Milestone not found"));
    }

    private void apply(Milestone milestone, MilestoneRequest request) {
        MilestoneStatus status = request.getStatus() == null ? MilestoneStatus.PLANNED : request.getStatus();
        if (status == MilestoneStatus.COMPLETED) {
            milestone.setCompletedDate(request.getCompletedDate() == null
                    ? LocalDate.now() : request.getCompletedDate());
        } else {
            milestone.setCompletedDate(null);
        }
        if (request.getCompletedDate() != null && status != MilestoneStatus.COMPLETED) {
            throw new BadRequestException("Completed date is only allowed for completed milestones");
        }
        milestone.setTitle(request.getTitle().trim());
        milestone.setDescription(clean(request.getDescription()));
        milestone.setTargetDate(request.getTargetDate());
        milestone.setStatus(status);
    }

    private MilestoneResponse toResponse(Milestone milestone) {
        long total = taskRepository.countByMilestoneId(milestone.getId());
        long done = taskRepository.countByMilestoneIdAndStatus(milestone.getId(), TaskStatus.DONE);
        return MilestoneResponse.from(milestone, total, done);
    }

    private static String clean(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
