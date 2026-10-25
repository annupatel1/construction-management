package com.construction.construction_management.service.impl;

import com.construction.construction_management.dto.ProgressPhotoRequest;
import com.construction.construction_management.dto.ProgressReportRequest;
import com.construction.construction_management.dto.ProgressReportResponse;
import com.construction.construction_management.entity.ProgressPhoto;
import com.construction.construction_management.entity.ProgressReport;
import com.construction.construction_management.entity.ProgressReportStatus;
import com.construction.construction_management.entity.Project;
import com.construction.construction_management.entity.RoleName;
import com.construction.construction_management.exception.BadRequestException;
import com.construction.construction_management.exception.ConflictException;
import com.construction.construction_management.exception.ResourceNotFoundException;
import com.construction.construction_management.repository.ProgressReportRepository;
import com.construction.construction_management.security.CurrentUser;
import com.construction.construction_management.service.AccessGuard;
import com.construction.construction_management.service.AuditService;
import com.construction.construction_management.service.ProgressReportService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProgressReportServiceImpl implements ProgressReportService {

    private final ProgressReportRepository reportRepository;
    private final CurrentUser currentUser;
    private final AccessGuard guard;
    private final AuditService auditService;

    public ProgressReportServiceImpl(ProgressReportRepository reportRepository,
                                     CurrentUser currentUser,
                                     AccessGuard guard,
                                     AuditService auditService) {
        this.reportRepository = reportRepository;
        this.currentUser = currentUser;
        this.guard = guard;
        this.auditService = auditService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProgressReportResponse> list(int projectId, Pageable pageable) {
        guard.projectForSiteRead(projectId);
        Pageable page = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
        if (currentUser.role() == RoleName.HOUSE_OWNER) {
            return reportRepository.findByProjectIdAndStatusOrderByReportDateDescIdDesc(
                            projectId, ProgressReportStatus.PUBLISHED, page)
                    .map(ProgressReportResponse::from)
                    .getContent();
        }
        return reportRepository.findByProjectIdOrderByReportDateDescIdDesc(projectId, page)
                .map(ProgressReportResponse::from)
                .getContent();
    }

    @Override
    @Transactional(readOnly = true)
    public ProgressReportResponse get(int projectId, int reportId) {
        guard.projectForSiteRead(projectId);
        ProgressReport report = load(projectId, reportId);
        if (currentUser.role() == RoleName.HOUSE_OWNER
                && report.getStatus() != ProgressReportStatus.PUBLISHED) {
            throw new ResourceNotFoundException("Progress report not found");
        }
        return ProgressReportResponse.from(report);
    }

    @Override
    @Transactional
    public ProgressReportResponse create(int projectId, ProgressReportRequest request) {
        Project project = guard.projectForSiteRead(projectId);
        requireReportAuthor();
        if (reportRepository.existsByProjectIdAndReportDate(projectId, request.getReportDate())) {
            throw new ConflictException("A progress report already exists for this date");
        }

        ProgressReport report = new ProgressReport();
        report.setProject(project);
        report.setAuthor(currentUser.get());
        apply(report, request);
        report.setStatus(ProgressReportStatus.DRAFT);
        report.setCreatedAt(LocalDateTime.now());
        report.setUpdatedAt(LocalDateTime.now());

        ProgressReport saved = reportRepository.save(report);
        auditService.record("PROGRESS_REPORT_CREATED", "ProgressReport", saved.getId(),
                "projectId=" + projectId + "; date=" + saved.getReportDate());
        return ProgressReportResponse.from(saved);
    }

    @Override
    @Transactional
    public ProgressReportResponse update(int projectId, int reportId, ProgressReportRequest request) {
        guard.projectForSiteRead(projectId);
        ProgressReport report = load(projectId, reportId);
        requireAuthorOrManager(report);
        if (report.getStatus() == ProgressReportStatus.PUBLISHED) {
            throw new BadRequestException("Published reports cannot be edited");
        }
        if (!report.getReportDate().equals(request.getReportDate())
                && reportRepository.existsByProjectIdAndReportDate(projectId, request.getReportDate())) {
            throw new ConflictException("A progress report already exists for this date");
        }

        apply(report, request);
        // A submitted report goes back to draft when its content changes, so the owner
        // never sees stale content that has not been deliberately published again.
        if (report.getStatus() == ProgressReportStatus.SUBMITTED) {
            report.setStatus(ProgressReportStatus.DRAFT);
        }
        report.setUpdatedAt(LocalDateTime.now());
        ProgressReport saved = reportRepository.save(report);
        auditService.record("PROGRESS_REPORT_UPDATED", "ProgressReport", saved.getId(),
                "projectId=" + projectId + "; date=" + saved.getReportDate());
        return ProgressReportResponse.from(saved);
    }

    @Override
    @Transactional
    public ProgressReportResponse submit(int projectId, int reportId) {
        guard.projectForSiteRead(projectId);
        ProgressReport report = load(projectId, reportId);
        requireAuthorOrManager(report);
        if (report.getStatus() == ProgressReportStatus.PUBLISHED) {
            throw new BadRequestException("This report is already published");
        }
        if (report.getStatus() == ProgressReportStatus.SUBMITTED) {
            return ProgressReportResponse.from(report);
        }
        report.setStatus(ProgressReportStatus.SUBMITTED);
        report.setUpdatedAt(LocalDateTime.now());
        ProgressReport saved = reportRepository.save(report);
        auditService.record("PROGRESS_REPORT_SUBMITTED", "ProgressReport", saved.getId(),
                "projectId=" + projectId);
        return ProgressReportResponse.from(saved);
    }

    @Override
    @Transactional
    public ProgressReportResponse publish(int projectId, int reportId) {
        guard.projectForSiteRead(projectId);
        ProgressReport report = load(projectId, reportId);
        if (currentUser.role() != RoleName.HOUSE_OWNER
                && currentUser.role() != RoleName.CONTRACTOR
                && !currentUser.isAdmin()) {
            throw new AccessDeniedException("Only the owner, contractor or an admin can publish reports");
        }
        if (report.getStatus() != ProgressReportStatus.SUBMITTED) {
            throw new BadRequestException("Submit the report before publishing it");
        }
        report.setStatus(ProgressReportStatus.PUBLISHED);
        report.setPublishedAt(LocalDateTime.now());
        report.setUpdatedAt(LocalDateTime.now());
        ProgressReport saved = reportRepository.save(report);
        auditService.record("PROGRESS_REPORT_PUBLISHED", "ProgressReport", saved.getId(),
                "projectId=" + projectId);
        return ProgressReportResponse.from(saved);
    }

    @Override
    @Transactional
    public void delete(int projectId, int reportId) {
        guard.projectForSiteRead(projectId);
        ProgressReport report = load(projectId, reportId);
        requireAuthorOrManager(report);
        if (report.getStatus() != ProgressReportStatus.DRAFT) {
            throw new BadRequestException("Only draft reports can be deleted");
        }
        reportRepository.delete(report);
        auditService.record("PROGRESS_REPORT_DELETED", "ProgressReport", reportId,
                "projectId=" + projectId);
    }

    private ProgressReport load(int projectId, int reportId) {
        return reportRepository.findByIdAndProjectId(reportId, projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Progress report not found"));
    }

    private void requireReportAuthor() {
        RoleName role = currentUser.role();
        if (role != RoleName.CONTRACTOR && role != RoleName.ENGINEER && role != RoleName.ADMIN) {
            throw new AccessDeniedException("Only the contractor, an engineer or an admin can write reports");
        }
    }

    private void requireAuthorOrManager(ProgressReport report) {
        RoleName role = currentUser.role();
        boolean manager = role == RoleName.CONTRACTOR || role == RoleName.ADMIN;
        if (!manager && report.getAuthor().getId() != currentUser.id()) {
            throw new AccessDeniedException("Only the author, contractor or an admin can change this report");
        }
    }

    private void apply(ProgressReport report, ProgressReportRequest request) {
        report.setReportDate(request.getReportDate());
        report.setPlannedWork(request.getPlannedWork().trim());
        report.setCompletedWork(request.getCompletedWork().trim());
        report.setBlockers(clean(request.getBlockers()));
        report.setNextDayPlan(clean(request.getNextDayPlan()));
        syncPhotos(report, request.getPhotos());
    }

    private void syncPhotos(ProgressReport report, List<ProgressPhotoRequest> requests) {
        report.getPhotos().clear();
        if (requests == null) {
            return;
        }
        int order = 0;
        for (ProgressPhotoRequest request : requests) {
            ProgressPhoto photo = new ProgressPhoto();
            photo.setReport(report);
            photo.setPhotoUrl(request.getPhotoUrl().trim());
            photo.setCaption(clean(request.getCaption()));
            photo.setTakenDate(request.getTakenDate());
            photo.setSortOrder(order++);
            photo.setCreatedAt(LocalDateTime.now());
            report.getPhotos().add(photo);
        }
    }

    private static String clean(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
