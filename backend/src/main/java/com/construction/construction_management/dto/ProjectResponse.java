package com.construction.construction_management.dto;

import com.construction.construction_management.entity.Project;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
public class ProjectResponse {
    int id;
    DirectoryUserResponse houseOwner;
    DirectoryUserResponse contractor;
    String title;
    String location;
    BigDecimal areaSqFt;
    BigDecimal ratePerSqFt;
    BigDecimal totalEstimatedCost;
    String contractType;
    String status;
    LocalDate startDate;
    LocalDate endDate;

    public static ProjectResponse from(Project p) {
        return new ProjectResponse(
                p.getId(),
                p.getHouseOwner() == null ? null : DirectoryUserResponse.from(p.getHouseOwner()),
                p.getContractor() == null ? null : DirectoryUserResponse.from(p.getContractor()),
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
