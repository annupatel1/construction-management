package com.construction.construction_management.dto;

import com.construction.construction_management.entity.IssuePhoto;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class IssuePhotoResponse {
    int id;
    String photoUrl;
    String caption;

    public static IssuePhotoResponse from(IssuePhoto photo) {
        return new IssuePhotoResponse(photo.getId(), photo.getPhotoUrl(), photo.getCaption());
    }
}
