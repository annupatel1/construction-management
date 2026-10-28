package com.construction.construction_management.dto;

import com.construction.construction_management.entity.LabourAssignment;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
public class LabourAssignmentResponse {
    int id;
    DirectoryUserResponse labour;
    ProjectResponse project;
    BigDecimal dailyRate;
    String status;
    BigDecimal advanceTaken;
    LocalDate assignedDate;

    public static LabourAssignmentResponse from(LabourAssignment a) {
        return new LabourAssignmentResponse(
                a.getId(),
                DirectoryUserResponse.from(a.getLabour()),
                ProjectResponse.from(a.getProject()),
                a.getDailyRate(),
                a.getStatus().toString(),
                a.getAdvanceTaken(),
                a.getAssignedDate()
        );
    }
}
