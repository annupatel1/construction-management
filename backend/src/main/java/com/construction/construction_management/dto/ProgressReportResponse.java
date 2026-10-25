package com.construction.construction_management.dto;

import com.construction.construction_management.entity.ProgressReport;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class ProgressReportResponse {
    int id;
    int projectId;
    SitePartyResponse author;
    LocalDate reportDate;
    String plannedWork;
    String completedWork;
    String blockers;
    String nextDayPlan;
    String status;
    LocalDateTime publishedAt;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    List<ProgressPhotoResponse> photos;

    public static ProgressReportResponse from(ProgressReport report) {
        return new ProgressReportResponse(
                report.getId(),
                report.getProject().getId(),
                SitePartyResponse.from(report.getAuthor()),
                report.getReportDate(),
                report.getPlannedWork(),
                report.getCompletedWork(),
                report.getBlockers(),
                report.getNextDayPlan(),
                report.getStatus().name(),
                report.getPublishedAt(),
                report.getCreatedAt(),
                report.getUpdatedAt(),
                report.getPhotos().stream().map(ProgressPhotoResponse::from).toList()
        );
    }
}
