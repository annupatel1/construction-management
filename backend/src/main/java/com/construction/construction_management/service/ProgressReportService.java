package com.construction.construction_management.service;

import com.construction.construction_management.dto.ProgressReportRequest;
import com.construction.construction_management.dto.ProgressReportResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProgressReportService {
    List<ProgressReportResponse> list(int projectId, Pageable pageable);

    ProgressReportResponse get(int projectId, int reportId);

    ProgressReportResponse create(int projectId, ProgressReportRequest request);

    ProgressReportResponse update(int projectId, int reportId, ProgressReportRequest request);

    ProgressReportResponse submit(int projectId, int reportId);

    ProgressReportResponse publish(int projectId, int reportId);

    void delete(int projectId, int reportId);
}
