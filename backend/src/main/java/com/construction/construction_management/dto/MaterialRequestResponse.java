package com.construction.construction_management.dto;

import com.construction.construction_management.entity.MaterialRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class MaterialRequestResponse {

    int id;
    int projectId;
    int materialId;
    String materialName;
    String materialUnit;
    BigDecimal quantity;
    LocalDate neededBy;
    String notes;
    String status;
    SitePartyResponse requestedBy;
    SitePartyResponse reviewedBy;
    LocalDateTime reviewedAt;
    String reviewNote;
    LocalDateTime createdAt;

    public static MaterialRequestResponse from(MaterialRequest request) {
        return new MaterialRequestResponse(
                request.getId(),
                request.getProject().getId(),
                request.getMaterial().getId(),
                request.getMaterial().getName(),
                request.getMaterial().getUnit(),
                request.getQuantity(),
                request.getNeededBy(),
                request.getNotes(),
                request.getStatus().name(),
                SitePartyResponse.from(request.getRequestedBy()),
                SitePartyResponse.from(request.getReviewedBy()),
                request.getReviewedAt(),
                request.getReviewNote(),
                request.getCreatedAt());
    }
}
