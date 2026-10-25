package com.construction.construction_management.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyOtpRequest {
    @NotBlank(message = "Enter your mobile number or email")
    String identifier;

    @NotBlank(message = "Enter the 6-digit code")
    @Pattern(regexp = "^\\d{6}$", message = "The code must be 6 digits")
    String otp;
}
