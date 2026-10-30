package com.construction.construction_management.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordRequest {
    @NotBlank(message = "Enter your mobile number or email")
    String identifier;

    @NotBlank(message = "Enter the 6-digit code")
    @Pattern(regexp = "^\\d{6}$", message = "The code must be 6 digits")
    String otp;

    @NotBlank(message = "New password is required")
    @Size(min = 8, max = 72, message = "Password must be 8 to 72 characters")
    String newPassword;
}
