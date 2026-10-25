package com.construction.construction_management.dto;

import com.construction.construction_management.entity.RoleName;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {

    @NotBlank(message = "Name is required")
    String name;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Enter a valid 10-digit Indian mobile number")
    String phone;

    @Email(message = "Invalid email format")
    String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    String password;

    @NotBlank(message = "Address is required")
    String address;

    @NotNull(message = "Role is required")
    RoleName roleName;
}
