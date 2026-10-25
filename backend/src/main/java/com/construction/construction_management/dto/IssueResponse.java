package com.construction.construction_management.dto;

import com.construction.construction_management.entity.ProjectIssue;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class IssueResponse {
    int id;
    int projectId;
    String title;
    String description;
    String category;
    String status;
    SitePartyResponse raisedBy;
    SitePartyResponse assignee;
    LocalDate dueDate;
    String resolutionNote;
    String resolutionPhotoUrl;
    LocalDateTime resolvedAt;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    List<IssuePhotoResponse> photos;
    List<IssueCommentResponse> comments;

    public static IssueResponse from(ProjectIssue issue) {
        return new IssueResponse(
                issue.getId(),
                issue.getProject().getId(),
                issue.getTitle(),
                issue.getDescription(),
                issue.getCategory().name(),
                issue.getStatus().name(),
                SitePartyResponse.from(issue.getRaisedBy()),
                SitePartyResponse.from(issue.getAssignee()),
                issue.getDueDate(),
                issue.getResolutionNote(),
                issue.getResolutionPhotoUrl(),
                issue.getResolvedAt(),
                issue.getCreatedAt(),
                issue.getUpdatedAt(),
                issue.getPhotos().stream().map(IssuePhotoResponse::from).toList(),
                issue.getComments().stream().map(IssueCommentResponse::from).toList()
        );
    }
}
