package com.construction.construction_management.service.impl;

import com.construction.construction_management.dto.IssueCommentRequest;
import com.construction.construction_management.dto.IssueCommentResponse;
import com.construction.construction_management.dto.IssuePhotoRequest;
import com.construction.construction_management.dto.IssueRequest;
import com.construction.construction_management.dto.IssueResponse;
import com.construction.construction_management.dto.IssueStatusRequest;
import com.construction.construction_management.entity.IssueCategory;
import com.construction.construction_management.entity.IssueComment;
import com.construction.construction_management.entity.IssuePhoto;
import com.construction.construction_management.entity.IssueStatus;
import com.construction.construction_management.entity.Project;
import com.construction.construction_management.entity.ProjectIssue;
import com.construction.construction_management.entity.RoleName;
import com.construction.construction_management.entity.User;
import com.construction.construction_management.exception.BadRequestException;
import com.construction.construction_management.exception.ResourceNotFoundException;
import com.construction.construction_management.repository.ProjectIssueRepository;
import com.construction.construction_management.repository.UserRepository;
import com.construction.construction_management.security.CurrentUser;
import com.construction.construction_management.service.AccessGuard;
import com.construction.construction_management.service.AuditService;
import com.construction.construction_management.service.SiteIssueService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SiteIssueServiceImpl implements SiteIssueService {

    private final ProjectIssueRepository issueRepository;
    private final UserRepository userRepository;
    private final CurrentUser currentUser;
    private final AccessGuard guard;
    private final AuditService auditService;

    public SiteIssueServiceImpl(ProjectIssueRepository issueRepository,
                                UserRepository userRepository,
                                CurrentUser currentUser,
                                AccessGuard guard,
                                AuditService auditService) {
        this.issueRepository = issueRepository;
        this.userRepository = userRepository;
        this.currentUser = currentUser;
        this.guard = guard;
        this.auditService = auditService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<IssueResponse> list(int projectId, Pageable pageable) {
        guard.projectForSiteRead(projectId);
        Pageable page = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
        return issueRepository.findByProjectIdOrderByCreatedAtDescIdDesc(projectId, page)
                .map(IssueResponse::from)
                .getContent();
    }

    @Override
    @Transactional(readOnly = true)
    public IssueResponse get(int projectId, int issueId) {
        guard.projectForSiteRead(projectId);
        return IssueResponse.from(load(projectId, issueId));
    }

    @Override
    @Transactional
    public IssueResponse create(int projectId, IssueRequest request) {
        Project project = guard.projectForSiteRead(projectId);
        ProjectIssue issue = new ProjectIssue();
        issue.setProject(project);
        issue.setRaisedBy(currentUser.get());
        issue.setCategory(request.getCategory() == null ? IssueCategory.QUALITY : request.getCategory());
        issue.setCreatedAt(LocalDateTime.now());
        issue.setUpdatedAt(LocalDateTime.now());
        apply(project, issue, request);
        issue.setStatus(issue.getAssignee() == null ? IssueStatus.OPEN : IssueStatus.ASSIGNED);
        ProjectIssue saved = issueRepository.save(issue);
        auditService.record("ISSUE_CREATED", "ProjectIssue", saved.getId(),
                "projectId=" + projectId + "; category=" + saved.getCategory());
        return IssueResponse.from(saved);
    }

    @Override
    @Transactional
    public IssueResponse update(int projectId, int issueId, IssueRequest request) {
        Project project = guard.projectForSiteRead(projectId);
        ProjectIssue issue = load(projectId, issueId);
        boolean manager = isManager();
        if (!manager && issue.getRaisedBy().getId() != currentUser.id()) {
            throw new AccessDeniedException("Only the raiser, contractor or an admin can edit this issue");
        }
        if (!manager && issue.getStatus() == IssueStatus.RESOLVED) {
            throw new BadRequestException("Resolved issues can only be changed by the contractor");
        }
        apply(project, issue, request);
        if (issue.getAssignee() != null && issue.getStatus() == IssueStatus.OPEN) {
            issue.setStatus(IssueStatus.ASSIGNED);
        }
        issue.setUpdatedAt(LocalDateTime.now());
        ProjectIssue saved = issueRepository.save(issue);
        auditService.record("ISSUE_UPDATED", "ProjectIssue", saved.getId(),
                "projectId=" + projectId);
        return IssueResponse.from(saved);
    }

    @Override
    @Transactional
    public IssueResponse updateStatus(int projectId, int issueId, IssueStatusRequest request) {
        guard.projectForSiteRead(projectId);
        ProjectIssue issue = load(projectId, issueId);
        boolean manager = isManager();
        boolean assignee = issue.getAssignee() != null && issue.getAssignee().getId() == currentUser.id();
        if (!manager && !assignee) {
            throw new AccessDeniedException("Only the owner, contractor, admin or assignee can change this issue");
        }
        if (request.getStatus() == IssueStatus.ASSIGNED && issue.getAssignee() == null) {
            throw new BadRequestException("Assign the issue to someone first");
        }
        if (request.getStatus() == IssueStatus.RESOLVED) {
            if (request.getResolutionNote() == null || request.getResolutionNote().isBlank()) {
                throw new BadRequestException("Add a resolution note before resolving the issue");
            }
            issue.setResolutionNote(request.getResolutionNote().trim());
            issue.setResolutionPhotoUrl(clean(request.getResolutionPhotoUrl()));
            issue.setResolvedAt(LocalDateTime.now());
        } else {
            issue.setResolvedAt(null);
            if (request.getStatus() == IssueStatus.REOPENED) {
                issue.setResolutionNote(null);
                issue.setResolutionPhotoUrl(null);
            }
        }
        issue.setStatus(request.getStatus());
        issue.setUpdatedAt(LocalDateTime.now());
        ProjectIssue saved = issueRepository.save(issue);
        auditService.record("ISSUE_STATUS_CHANGED", "ProjectIssue", saved.getId(),
                "projectId=" + projectId + "; status=" + saved.getStatus());
        return IssueResponse.from(saved);
    }

    @Override
    @Transactional
    public IssueCommentResponse addComment(int projectId, int issueId, IssueCommentRequest request) {
        guard.projectForSiteRead(projectId);
        ProjectIssue issue = load(projectId, issueId);
        IssueComment comment = new IssueComment();
        comment.setIssue(issue);
        comment.setAuthor(currentUser.get());
        comment.setMessage(request.getMessage().trim());
        comment.setCreatedAt(LocalDateTime.now());
        issue.getComments().add(comment);
        issue.setUpdatedAt(LocalDateTime.now());
        issueRepository.save(issue);
        auditService.record("ISSUE_COMMENTED", "ProjectIssue", issueId, "projectId=" + projectId);
        return IssueCommentResponse.from(comment);
    }

    @Override
    @Transactional
    public void delete(int projectId, int issueId) {
        ProjectIssue issue = load(projectId, issueId);
        if (!isManager()) {
            throw new AccessDeniedException("Only the contractor or an admin can delete an issue");
        }
        issueRepository.delete(issue);
        auditService.record("ISSUE_DELETED", "ProjectIssue", issueId, "projectId=" + projectId);
    }

    private ProjectIssue load(int projectId, int issueId) {
        guard.projectForSiteRead(projectId);
        return issueRepository.findByIdAndProjectId(issueId, projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found"));
    }

    private boolean isManager() {
        RoleName role = currentUser.role();
        return role == RoleName.CONTRACTOR || role == RoleName.ADMIN || role == RoleName.HOUSE_OWNER;
    }

    private void apply(Project project, ProjectIssue issue, IssueRequest request) {
        User assignee = null;
        if (request.getAssigneeId() != null) {
            assignee = userRepository.findById(request.getAssigneeId())
                    .orElseThrow(() -> new BadRequestException("Assignee not found"));
            if (!guard.isAssignable(project, assignee.getId())) {
                throw new BadRequestException("Assignee is not part of this project");
            }
        }
        issue.setTitle(request.getTitle().trim());
        issue.setDescription(clean(request.getDescription()));
        issue.setCategory(request.getCategory() == null ? IssueCategory.QUALITY : request.getCategory());
        issue.setAssignee(assignee);
        issue.setDueDate(request.getDueDate());
        if (issue.getStatus() == null) {
            issue.setStatus(assignee == null ? IssueStatus.OPEN : IssueStatus.ASSIGNED);
        }
        if (request.getPhotos() != null) {
            syncPhotos(issue, request.getPhotos());
        }
    }

    private void syncPhotos(ProjectIssue issue, List<IssuePhotoRequest> requests) {
        issue.getPhotos().clear();
        if (requests == null) {
            return;
        }
        for (IssuePhotoRequest request : requests) {
            IssuePhoto photo = new IssuePhoto();
            photo.setIssue(issue);
            photo.setPhotoUrl(request.getPhotoUrl().trim());
            photo.setCaption(clean(request.getCaption()));
            photo.setCreatedAt(LocalDateTime.now());
            issue.getPhotos().add(photo);
        }
    }

    private static String clean(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
