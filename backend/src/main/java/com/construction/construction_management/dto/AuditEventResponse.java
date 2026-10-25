package com.construction.construction_management.dto;

import com.construction.construction_management.entity.AuditEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class AuditEventResponse {
    int id;
    Integer actorId;
    String actorRole;
    String action;
    String entityType;
    Integer entityId;
    String details;
    LocalDateTime createdAt;

    public static AuditEventResponse from(AuditEvent event) {
        return new AuditEventResponse(
                event.getId(),
                event.getActorId(),
                event.getActorRole(),
                event.getAction(),
                event.getEntityType(),
                event.getEntityId(),
                event.getDetails(),
                event.getCreatedAt()
        );
    }
}
