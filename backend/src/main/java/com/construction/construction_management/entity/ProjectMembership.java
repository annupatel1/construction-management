package com.construction.construction_management.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/** Grants an accepted engineer scoped access to one project. Pending invitations do not create one. */
@Getter
@Setter
@Entity
@Table(name = "project_membership",
        uniqueConstraints = @UniqueConstraint(name = "uk_membership_project_user",
                columnNames = {"project_id", "user_id"}))
public class ProjectMembership {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    Project project;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "member_role", nullable = false, length = 32)
    ProjectMemberRole role;

    @Column(length = 255)
    String permissions;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invited_by_id")
    User invitedBy;

    @Column(nullable = false)
    boolean active = true;

    @Column(nullable = false)
    LocalDateTime createdAt;

    LocalDateTime removedAt;
}
