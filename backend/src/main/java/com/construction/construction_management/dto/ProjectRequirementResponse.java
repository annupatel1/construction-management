package com.construction.construction_management.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
public class ProjectRequirementResponse {

    int id;
    DirectoryUserResponse houseOwner;
    String city;
    String area;
    String pincode;
    BigDecimal areaSqFt;
    String contractType;
    String description;
    String status;
    LocalDate postedDate;

}