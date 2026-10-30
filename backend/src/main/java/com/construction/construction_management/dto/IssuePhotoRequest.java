package com.construction.construction_management.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IssuePhotoRequest {
    @NotBlank(message = "Photo URL is required")
    @Size(max = 1000, message = "Photo URL is too long")
    @Pattern(regexp = "^https?://.+", message = "Photo URL must start with http:// or https://")
    String photoUrl;

    @Size(max = 255, message = "Photo caption is too long")
    String caption;
}
