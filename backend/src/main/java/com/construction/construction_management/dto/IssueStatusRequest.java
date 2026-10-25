package com.construction.construction_management.dto;

import com.construction.construction_management.entity.IssueStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IssueStatusRequest {
    @NotNull(message = "Issue status is required")
    IssueStatus status;

    @Size(max = 2000, message = "Resolution note is too long")
    String resolutionNote;

    @Size(max = 1000, message = "Resolution photo URL is too long")
    @Pattern(regexp = "^$|^https?://.+", message = "Resolution photo URL must start with http:// or https://")
    String resolutionPhotoUrl;
}
