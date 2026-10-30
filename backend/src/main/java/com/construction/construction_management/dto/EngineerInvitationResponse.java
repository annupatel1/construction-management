package com.construction.construction_management.dto;

import com.construction.construction_management.entity.EngineerInvitation;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EngineerInvitationResponse {
    int id;
    int projectId;
    String projectTitle;
    String projectLocation;
    InvitationPartyResponse inviter;
    InvitationPartyResponse engineer;
    String scope;
    LocalDate startDate;
    LocalDate endDate;
    String feeTerms;
    String requestedPermissions;
    String status;
    LocalDate responseDeadline;
    String responseMessage;
    LocalDateTime createdAt;
    LocalDateTime respondedAt;

    public static EngineerInvitationResponse from(EngineerInvitation i) {
        return new EngineerInvitationResponse(
                i.getId(),
                i.getProject().getId(),
                i.getProject().getTitle(),
                i.getProject().getLocation(),
                InvitationPartyResponse.from(i.getInviter()),
                InvitationPartyResponse.from(i.getEngineer()),
                i.getScope(),
                i.getStartDate(),
                i.getEndDate(),
                i.getFeeTerms(),
                i.getRequestedPermissions(),
                i.getStatus().name(),
                i.getResponseDeadline(),
                i.getResponseMessage(),
                i.getCreatedAt(),
                i.getRespondedAt()
        );
    }
}
