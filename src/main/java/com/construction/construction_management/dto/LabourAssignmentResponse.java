package com.construction.construction_management.dto;

import com.construction.construction_management.entity.LabourAssignment;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
public class LabourAssignmentResponse {
    int id;
    UserResponse labour;
    ProjectResponse project;
    double dailyRate;
    String status;
    double advanceTaken;
    LocalDate assignedDate;

    public static LabourAssignmentResponse from(LabourAssignment a) {
        return new LabourAssignmentResponse(
                a.getId(),
                UserResponse.from(a.getLabour()),
                ProjectResponse.from(a.getProject()),
                a.getDailyRate(),
                a.getStatus().toString(),
                a.getAdvanceTaken(),
                a.getAssignedDate()
        );
    }
}
