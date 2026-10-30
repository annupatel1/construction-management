package com.construction.construction_management.service.impl;

import com.construction.construction_management.entity.AuditEvent;
import com.construction.construction_management.entity.User;
import com.construction.construction_management.repository.AuditEventRepository;
import com.construction.construction_management.security.CurrentUser;
import com.construction.construction_management.service.AuditService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuditServiceImpl implements AuditService {

    private static final int MAX_DETAILS = 1000;

    private final AuditEventRepository auditEventRepository;
    private final CurrentUser currentUser;

    public AuditServiceImpl(AuditEventRepository auditEventRepository, CurrentUser currentUser) {
        this.auditEventRepository = auditEventRepository;
        this.currentUser = currentUser;
    }

    @Override
    public void record(String action, String entityType, Integer entityId, String details) {
        AuditEvent event = new AuditEvent();
        event.setAction(action);
        event.setEntityType(entityType);
        event.setEntityId(entityId);
        event.setDetails(truncate(details));
        event.setCreatedAt(LocalDateTime.now());

        try {
            User actor = currentUser.get();
            event.setActorId(actor.getId());
            event.setActorRole(actor.getRole().getName().name());
        } catch (AccessDeniedException ignored) {
            // system action (for example a scheduled or auth flow) with no logged-in actor
        }

        auditEventRepository.save(event);
    }

    private static String truncate(String details) {
        if (details == null || details.length() <= MAX_DETAILS) {
            return details;
        }
        return details.substring(0, MAX_DETAILS);
    }
}
