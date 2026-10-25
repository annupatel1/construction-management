package com.construction.construction_management.dto;

import com.construction.construction_management.entity.IssueComment;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class IssueCommentResponse {
    int id;
    SitePartyResponse author;
    String message;
    LocalDateTime createdAt;

    public static IssueCommentResponse from(IssueComment comment) {
        return new IssueCommentResponse(comment.getId(), SitePartyResponse.from(comment.getAuthor()),
                comment.getMessage(), comment.getCreatedAt());
    }
}
