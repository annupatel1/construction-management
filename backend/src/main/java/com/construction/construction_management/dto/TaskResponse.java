package com.construction.construction_management.dto;

import com.construction.construction_management.entity.ProjectTask;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class TaskResponse {
    int id;
    int projectId;
    Integer milestoneId;
    String milestoneTitle;
    String title;
    String description;
    SitePartyResponse assignee;
    LocalDate dueDate;
    String status;
    String delayReason;
    boolean overdue;
    LocalDateTime completedAt;
    SitePartyResponse createdBy;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;

    public static TaskResponse from(ProjectTask task) {
        return new TaskResponse(
                task.getId(),
                task.getProject().getId(),
                task.getMilestone() == null ? null : task.getMilestone().getId(),
                task.getMilestone() == null ? null : task.getMilestone().getTitle(),
                task.getTitle(),
                task.getDescription(),
                SitePartyResponse.from(task.getAssignee()),
                task.getDueDate(),
                task.getStatus().name(),
                task.getDelayReason(),
                task.getStatus() != com.construction.construction_management.entity.TaskStatus.DONE
                        && task.getDueDate() != null && task.getDueDate().isBefore(LocalDate.now()),
                task.getCompletedAt(),
                SitePartyResponse.from(task.getCreatedBy()),
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
    }
}
