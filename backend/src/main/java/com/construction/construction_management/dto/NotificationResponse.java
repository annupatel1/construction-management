package com.construction.construction_management.dto;

import com.construction.construction_management.entity.Notification;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class NotificationResponse {

    int id;
    String type;
    String title;
    String body;
    String link;
    boolean read;
    LocalDateTime createdAt;
    Integer projectId;
    String projectTitle;

    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getType().name(),
                notification.getTitle(),
                notification.getBody(),
                notification.getLink(),
                notification.getReadAt() != null,
                notification.getCreatedAt(),
                notification.getProject() == null ? null : notification.getProject().getId(),
                notification.getProject() == null ? null : notification.getProject().getTitle()
        );
    }
}
