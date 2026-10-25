package com.construction.construction_management.dto;

import com.construction.construction_management.entity.ProgressPhoto;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class ProgressPhotoResponse {
    int id;
    String photoUrl;
    String caption;
    LocalDate takenDate;
    int sortOrder;

    public static ProgressPhotoResponse from(ProgressPhoto photo) {
        return new ProgressPhotoResponse(photo.getId(), photo.getPhotoUrl(), photo.getCaption(),
                photo.getTakenDate(), photo.getSortOrder());
    }
}
