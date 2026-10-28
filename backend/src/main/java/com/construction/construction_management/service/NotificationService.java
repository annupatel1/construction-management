package com.construction.construction_management.service;

import com.construction.construction_management.dto.NotificationResponse;
import com.construction.construction_management.entity.NotificationType;
import com.construction.construction_management.entity.Project;
import com.construction.construction_management.entity.User;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface NotificationService {

    void send(User recipient, Project project, NotificationType type,
              String title, String body, String link);

    List<NotificationResponse> list(Pageable pageable);

    long unreadCount();

    NotificationResponse markRead(int notificationId);

    void markAllRead();
}
