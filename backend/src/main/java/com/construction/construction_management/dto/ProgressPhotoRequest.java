package com.construction.construction_management.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class ProgressPhotoRequest {
    @NotBlank(message = "Photo URL is required")
    @Size(max = 1000, message = "Photo URL is too long")
    @Pattern(regexp = "^https?://.+", message = "Photo URL must start with http:// or https://")
    String photoUrl;

    @Size(max = 255, message = "Photo caption is too long")
    String caption;

    LocalDate takenDate;
}
