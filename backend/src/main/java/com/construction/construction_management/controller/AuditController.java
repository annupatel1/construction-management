package com.construction.construction_management.controller;

import com.construction.construction_management.dto.AuditEventResponse;
import com.construction.construction_management.repository.AuditEventRepository;
import com.construction.construction_management.util.Pages;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/audit")
public class AuditController {

    private final AuditEventRepository auditEventRepository;

    public AuditController(AuditEventRepository auditEventRepository) {
        this.auditEventRepository = auditEventRepository;
    }

    /** Newest first. Admin-only; the shape is intentionally read-only. */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<AuditEventResponse> recent(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return auditEventRepository.findAll(Pages.of(page, size)).stream()
                .map(AuditEventResponse::from)
                .toList();
    }
}
