package com.construction.construction_management.controller;

import com.construction.construction_management.dto.EngineerInvitationRequest;
import com.construction.construction_management.dto.EngineerInvitationResponse;
import com.construction.construction_management.dto.InvitationDecisionRequest;
import com.construction.construction_management.dto.MessageResponse;
import com.construction.construction_management.dto.ProjectEngineerResponse;
import com.construction.construction_management.service.EngineerInvitationService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}")
public class ProjectEngineerController {

    private final EngineerInvitationService invitationService;

    public ProjectEngineerController(EngineerInvitationService invitationService) {
        this.invitationService = invitationService;
    }

    @PreAuthorize("hasAnyRole('HOUSE_OWNER','CONTRACTOR','ADMIN')")
    @PostMapping("/engineer-invitations")
    public EngineerInvitationResponse invite(@PathVariable int projectId,
                                             @Valid @RequestBody EngineerInvitationRequest request) {
        return invitationService.invite(projectId, request);
    }

    @PreAuthorize("hasAnyRole('HOUSE_OWNER','CONTRACTOR','ADMIN')")
    @GetMapping("/engineers")
    public List<ProjectEngineerResponse> members(@PathVariable int projectId) {
        return invitationService.members(projectId);
    }

    @PreAuthorize("hasAnyRole('HOUSE_OWNER','CONTRACTOR','ADMIN')")
    @DeleteMapping("/engineers/{membershipId}")
    public MessageResponse remove(@PathVariable int projectId, @PathVariable int membershipId) {
        invitationService.removeMember(projectId, membershipId);
        return new MessageResponse("Engineer removed from the project");
    }
}
