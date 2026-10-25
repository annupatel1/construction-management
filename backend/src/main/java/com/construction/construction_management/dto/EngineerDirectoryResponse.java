package com.construction.construction_management.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * Public engineer card. Phone, email, and login data are intentionally absent; they are disclosed
 * only through an accepted invitation/project membership.
 */
@Getter
@AllArgsConstructor
public class EngineerDirectoryResponse {
    int id;
    int userId;
    String name;
    String specialty;
    List<String> skills;
    int experienceYears;
    String city;
    String serviceArea;
    List<String> languages;
    String availability;
    String bio;
    String portfolioUrl;
    String credentialStatus;
    boolean profileComplete;
}
