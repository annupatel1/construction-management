package com.construction.construction_management.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "issue_photo")
public class IssuePhoto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "issue_id", nullable = false)
    ProjectIssue issue;

    @Column(name = "photo_url", nullable = false, length = 1000)
    String photoUrl;

    @Column(length = 255)
    String caption;

    @Column(nullable = false)
    LocalDateTime createdAt;
}
