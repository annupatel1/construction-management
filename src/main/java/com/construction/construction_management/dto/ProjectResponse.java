package com.construction.construction_management.dto;

import com.construction.construction_management.entity.Project;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
public class ProjectResponse {
    int id;
    UserResponse houseOwner;
    UserResponse contractor;
    String title;
    String location;
    double areaSqFt;
    double ratePerSqFt;
    double totalEstimatedCost;
    String contractType;
    String status;
    LocalDate startDate;
    LocalDate endDate;

    public static ProjectResponse from(Project p) {
        return new ProjectResponse(
                p.getId(),
                p.getHouseOwner() == null ? null : UserResponse.from(p.getHouseOwner()),
                p.getContractor() == null ? null : UserResponse.from(p.getContractor()),
                p.getTitle(),
                p.getLocation(),
                p.getAreaSqFt(),
                p.getRatePerSqFt(),
                p.getTotalEstimatedCost(),
                p.getContractType().toString(),
                p.getStatus().toString(),
                p.getStartDate(),
                p.getEndDate()
        );
    }
}
