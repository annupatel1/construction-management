package com.construction.construction_management.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
public class ProjectRequirementResponse {

    int id;
    UserResponse houseOwner;
    String city;
    String area;
    String pincode;
    double areaSqFt;
    String contractType;
    String description;
    String status;
    LocalDate postedDate;

}