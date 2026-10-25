package com.construction.construction_management.dto;

import com.construction.construction_management.entity.StockMovement;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class StockMovementResponse {

    int id;
    int materialId;
    String materialName;
    String materialUnit;
    String movementType;
    BigDecimal quantity;
    BigDecimal unitCost;
    LocalDate movementDate;
    String referenceType;
    Integer referenceId;
    String notes;
    SitePartyResponse createdBy;
    LocalDateTime createdAt;

    public static StockMovementResponse from(StockMovement movement) {
        return from(movement, true);
    }

    public static StockMovementResponse from(StockMovement movement, boolean includeCost) {
        return new StockMovementResponse(
                movement.getId(),
                movement.getMaterial().getId(),
                movement.getMaterial().getName(),
                movement.getMaterial().getUnit(),
                movement.getMovementType().name(),
                movement.getQuantity(),
                includeCost ? movement.getUnitCost() : null,
                movement.getMovementDate(),
                movement.getReferenceType(),
                movement.getReferenceId(),
                movement.getNotes(),
                SitePartyResponse.from(movement.getCreatedBy()),
                movement.getCreatedAt());
    }
}
