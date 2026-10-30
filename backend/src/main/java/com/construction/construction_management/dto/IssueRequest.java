package com.construction.construction_management.dto;

import com.construction.construction_management.entity.IssueCategory;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class IssueRequest {
    @NotBlank(message = "Issue title is required")
    @Size(max = 255, message = "Issue title is too long")
    String title;

    @Size(max = 2000, message = "Issue description is too long")
    String description;

    IssueCategory category;

    Integer assigneeId;

    LocalDate dueDate;

    @Valid
    @Size(max = 10, message = "Add at most 10 photos")
    List<IssuePhotoRequest> photos;
}
