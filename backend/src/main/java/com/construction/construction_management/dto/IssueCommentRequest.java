package com.construction.construction_management.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IssueCommentRequest {
    @NotBlank(message = "Comment is required")
    @Size(max = 2000, message = "Comment is too long")
    String message;
}
