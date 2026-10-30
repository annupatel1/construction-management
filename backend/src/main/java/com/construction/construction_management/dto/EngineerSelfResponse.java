package com.construction.construction_management.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/** The engineer's own profile, including their own contact details. */
@Getter
@AllArgsConstructor
public class EngineerSelfResponse {
    int id;
    int userId;
    String name;
    String phone;
    String email;
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
