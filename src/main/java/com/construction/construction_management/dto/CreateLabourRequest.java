package com.construction.construction_management.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

/** A contractor can add a worker who does not use the app yet. */
@Getter
@Setter
public class CreateLabourRequest {
    @NotBlank(message = "Name is required")
    String name;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Enter a valid 10-digit Indian mobile number")
    String phone;

    String address;
}
