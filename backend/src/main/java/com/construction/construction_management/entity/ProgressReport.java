package com.construction.construction_management.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "progress_report",
        uniqueConstraints = @UniqueConstraint(name = "uk_progress_report_project_date",
                columnNames = {"project_id", "report_date"}))
public class ProgressReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    Project project;

    @ManyToOne(optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    User author;

    @Column(name = "report_date", nullable = false)
    LocalDate reportDate;

    @Column(name = "planned_work", nullable = false, length = 2000)
    String plannedWork;

    @Column(name = "completed_work", nullable = false, length = 2000)
    String completedWork;

    @Column(length = 2000)
    String blockers;

    @Column(name = "next_day_plan", length = 2000)
    String nextDayPlan;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    ProgressReportStatus status;

    LocalDateTime publishedAt;

    @Column(nullable = false)
    LocalDateTime createdAt;

    @Column(nullable = false)
    LocalDateTime updatedAt;

    @BatchSize(size = 25)
    @OrderBy("sortOrder ASC, id ASC")
    @OneToMany(mappedBy = "report", cascade = CascadeType.ALL, orphanRemoval = true)
    List<ProgressPhoto> photos = new ArrayList<>();
}
