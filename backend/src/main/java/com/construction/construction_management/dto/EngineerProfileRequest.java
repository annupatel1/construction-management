package com.construction.construction_management.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EngineerProfileRequest {

    @NotBlank(message = "Specialty is required")
    @Size(max = 100, message = "Specialty is too long")
    String specialty;

    @Size(max = 500, message = "Skills are too long")
    String skills;

    @NotNull(message = "Experience is required")
    @Min(value = 0, message = "Experience cannot be negative")
    @Max(value = 60, message = "Experience must be 60 years or less")
    Integer experienceYears;

    @NotBlank(message = "City is required")
    @Size(max = 100, message = "City is too long")
    String city;

    @Size(max = 255, message = "Service area is too long")
    String serviceArea;

    @Size(max = 255, message = "Languages are too long")
    String languages;

    @NotNull(message = "Availability is required")
    com.construction.construction_management.entity.EngineerAvailability availability;

    @Size(max = 2000, message = "Bio is too long")
    String bio;

    @Size(max = 500, message = "Portfolio link is too long")
    @Pattern(regexp = "^$|^https?://.+", message = "Portfolio link must start with http:// or https://")
    String portfolioUrl;
}
