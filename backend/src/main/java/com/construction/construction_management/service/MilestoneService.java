package com.construction.construction_management.service;

import com.construction.construction_management.dto.MilestoneRequest;
import com.construction.construction_management.dto.MilestoneResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MilestoneService {
    List<MilestoneResponse> list(int projectId, Pageable pageable);

    MilestoneResponse create(int projectId, MilestoneRequest request);

    MilestoneResponse update(int projectId, int milestoneId, MilestoneRequest request);

    void delete(int projectId, int milestoneId);
}
