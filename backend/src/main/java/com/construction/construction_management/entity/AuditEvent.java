package com.construction.construction_management.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/** Append-only record of money and status changes, readable by admins. */
@Getter
@Setter
@Entity
@Table(name = "audit_event")
public class AuditEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;

    Integer actorId;
    String actorRole;

    @Column(nullable = false, length = 64)
    String action;

    @Column(length = 64)
    String entityType;

    Integer entityId;

    @Column(length = 1000)
    String details;

    @Column(nullable = false)
    LocalDateTime createdAt;
}
