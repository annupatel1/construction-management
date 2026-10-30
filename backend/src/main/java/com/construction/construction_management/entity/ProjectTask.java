package com.construction.construction_management.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "project_task")
public class ProjectTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    Project project;

    @ManyToOne
    @JoinColumn(name = "milestone_id")
    Milestone milestone;

    @Column(nullable = false, length = 255)
    String title;

    @Column(length = 2000)
    String description;

    @ManyToOne
    @JoinColumn(name = "assignee_id")
    User assignee;

    LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    TaskStatus status;

    @Column(name = "delay_reason", length = 1000)
    String delayReason;

    LocalDateTime completedAt;

    @ManyToOne(optional = false)
    @JoinColumn(name = "created_by_id", nullable = false)
    User createdBy;

    @Column(nullable = false)
    LocalDateTime createdAt;

    @Column(nullable = false)
    LocalDateTime updatedAt;
}
