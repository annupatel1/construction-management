package com.construction.construction_management.service;

import com.construction.construction_management.dto.EngineerInvitationRequest;
import com.construction.construction_management.dto.EngineerInvitationResponse;
import com.construction.construction_management.dto.ProjectEngineerResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface EngineerInvitationService {
    EngineerInvitationResponse invite(int projectId, EngineerInvitationRequest request);

    List<EngineerInvitationResponse> inbox(Pageable pageable);

    List<EngineerInvitationResponse> sent(Pageable pageable);

    EngineerInvitationResponse accept(int invitationId);

    EngineerInvitationResponse decline(int invitationId, String responseMessage);

    EngineerInvitationResponse cancel(int invitationId);

    List<ProjectEngineerResponse> members(int projectId);

    void removeMember(int projectId, int membershipId);
}
