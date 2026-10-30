package com.construction.construction_management.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** An owner/contractor asks an engineer to join a project. Access is granted only on acceptance. */
@Getter
@Setter
@Entity
@Table(name = "engineer_invitation")
public class EngineerInvitation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    Project project;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "inviter_id", nullable = false)
    User inviter;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "engineer_id", nullable = false)
    User engineer;

    @Column(nullable = false, length = 2000)
    String scope;

    LocalDate startDate;

    LocalDate endDate;

    @Column(length = 1000)
    String feeTerms;

    /** Comma-separated permissions the engineer will receive on acceptance. */
    @Column(length = 255)
    String requestedPermissions;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    InvitationStatus status = InvitationStatus.PENDING;

    LocalDate responseDeadline;

    @Column(length = 500)
    String responseMessage;

    @Column(nullable = false)
    LocalDateTime createdAt;

    LocalDateTime respondedAt;
}
