package com.construction.construction_management.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {
    /** Mobile number or email. */
    @NotBlank(message = "Enter your mobile number or email")
    String identifier;

    @NotBlank(message = "Password is required")
    String password;
}
