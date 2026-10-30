package com.construction.construction_management.controller;

import com.construction.construction_management.dto.EngineerInvitationResponse;
import com.construction.construction_management.dto.InvitationDecisionRequest;
import com.construction.construction_management.service.EngineerInvitationService;
import com.construction.construction_management.util.Pages;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/engineer-invitations")
public class EngineerInvitationController {

    private final EngineerInvitationService invitationService;

    public EngineerInvitationController(EngineerInvitationService invitationService) {
        this.invitationService = invitationService;
    }

    @PreAuthorize("hasRole('ENGINEER')")
    @GetMapping("/inbox")
    public List<EngineerInvitationResponse> inbox(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return invitationService.inbox(Pages.of(page, size));
    }

    @PreAuthorize("hasAnyRole('HOUSE_OWNER','CONTRACTOR','ADMIN')")
    @GetMapping("/sent")
    public List<EngineerInvitationResponse> sent(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return invitationService.sent(Pages.of(page, size));
    }

    @PreAuthorize("hasRole('ENGINEER')")
    @PostMapping("/{id}/accept")
    public EngineerInvitationResponse accept(@PathVariable int id) {
        return invitationService.accept(id);
    }

    @PreAuthorize("hasRole('ENGINEER')")
    @PostMapping("/{id}/decline")
    public EngineerInvitationResponse decline(@PathVariable int id,
                                              @Valid @RequestBody(required = false) InvitationDecisionRequest request) {
        return invitationService.decline(id, request == null ? null : request.getResponseMessage());
    }

    @PreAuthorize("hasAnyRole('HOUSE_OWNER','CONTRACTOR','ADMIN')")
    @PostMapping("/{id}/cancel")
    public EngineerInvitationResponse cancel(@PathVariable int id) {
        return invitationService.cancel(id);
    }
}
