package com.construction.construction_management.dto;

import com.construction.construction_management.entity.Milestone;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class MilestoneResponse {
    int id;
    int projectId;
    String title;
    String description;
    LocalDate targetDate;
    String status;
    LocalDate completedDate;
    long taskCount;
    long completedTaskCount;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;

    public static MilestoneResponse from(Milestone milestone, long taskCount, long completedTaskCount) {
        return new MilestoneResponse(
                milestone.getId(),
                milestone.getProject().getId(),
                milestone.getTitle(),
                milestone.getDescription(),
                milestone.getTargetDate(),
                milestone.getStatus().name(),
                milestone.getCompletedDate(),
                taskCount,
                completedTaskCount,
                milestone.getCreatedAt(),
                milestone.getUpdatedAt()
        );
    }
}
