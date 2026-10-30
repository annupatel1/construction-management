package com.construction.construction_management.service;

import com.construction.construction_management.dto.IssueCommentRequest;
import com.construction.construction_management.dto.IssueCommentResponse;
import com.construction.construction_management.dto.IssueRequest;
import com.construction.construction_management.dto.IssueResponse;
import com.construction.construction_management.dto.IssueStatusRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SiteIssueService {
    List<IssueResponse> list(int projectId, Pageable pageable);

    IssueResponse get(int projectId, int issueId);

    IssueResponse create(int projectId, IssueRequest request);

    IssueResponse update(int projectId, int issueId, IssueRequest request);

    IssueResponse updateStatus(int projectId, int issueId, IssueStatusRequest request);

    IssueCommentResponse addComment(int projectId, int issueId, IssueCommentRequest request);

    void delete(int projectId, int issueId);
}
