package com.construction.construction_management.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

/** An accepted engineer on the project team, with contact details visible to the owner/contractor. */
@Getter
@AllArgsConstructor
public class ProjectEngineerResponse {
    int membershipId;
    int userId;
    String name;
    String phone;
    String email;
    String specialty;
    int experienceYears;
    String city;
    String role;
    String permissions;
    LocalDateTime joinedAt;
}
