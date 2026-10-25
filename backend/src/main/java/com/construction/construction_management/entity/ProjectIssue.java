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
@Table(name = "project_issue")
public class ProjectIssue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    Project project;

    @ManyToOne(optional = false)
    @JoinColumn(name = "raised_by_id", nullable = false)
    User raisedBy;

    @ManyToOne
    @JoinColumn(name = "assignee_id")
    User assignee;

    @Column(nullable = false, length = 255)
    String title;

    @Column(length = 2000)
    String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    IssueCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    IssueStatus status;

    LocalDate dueDate;

    @Column(name = "resolution_note", length = 2000)
    String resolutionNote;

    @Column(name = "resolution_photo_url", length = 1000)
    String resolutionPhotoUrl;

    LocalDateTime resolvedAt;

    @Column(nullable = false)
    LocalDateTime createdAt;

    @Column(nullable = false)
    LocalDateTime updatedAt;

    @BatchSize(size = 25)
    @OrderBy("id ASC")
    @OneToMany(mappedBy = "issue", cascade = CascadeType.ALL, orphanRemoval = true)
    List<IssuePhoto> photos = new ArrayList<>();

    @BatchSize(size = 25)
    @OrderBy("createdAt ASC, id ASC")
    @OneToMany(mappedBy = "issue", cascade = CascadeType.ALL, orphanRemoval = true)
    List<IssueComment> comments = new ArrayList<>();
}
