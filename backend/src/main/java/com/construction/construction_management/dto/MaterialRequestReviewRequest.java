package com.construction.construction_management.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MaterialRequestReviewRequest {

    @Size(max = 1000, message = "Review note is too long")
    String reviewNote;
}
