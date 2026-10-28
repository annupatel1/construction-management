package com.construction.construction_management.service.impl;

import com.construction.construction_management.dto.NotificationResponse;
import com.construction.construction_management.entity.Notification;
import com.construction.construction_management.entity.NotificationType;
import com.construction.construction_management.entity.Project;
import com.construction.construction_management.entity.User;
import com.construction.construction_management.exception.ResourceNotFoundException;
import com.construction.construction_management.repository.NotificationRepository;
import com.construction.construction_management.security.CurrentUser;
import com.construction.construction_management.service.NotificationService;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final CurrentUser currentUser;

    public NotificationServiceImpl(NotificationRepository notificationRepository,
                                   CurrentUser currentUser) {
        this.notificationRepository = notificationRepository;
        this.currentUser = currentUser;
    }

    @Override
    @Transactional
    public void send(User recipient, Project project, NotificationType type,
                     String title, String body, String link) {
        if (recipient == null || recipient.getId() == currentUser.id()) {
            return;
        }
        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setProject(project);
        notification.setType(type);
        notification.setTitle(cut(title, 255));
        notification.setBody(cut(body, 1000));
        notification.setLink(cut(link, 512));
        notification.setCreatedAt(LocalDateTime.now());
        notificationRepository.save(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> list(Pageable pageable) {
        return notificationRepository
                .findByRecipientIdOrderByCreatedAtDescIdDesc(currentUser.id(), pageable)
                .map(NotificationResponse::from)
                .getContent();
    }

    @Override
    @Transactional(readOnly = true)
    public long unreadCount() {
        return notificationRepository.countByRecipientIdAndReadAtIsNull(currentUser.id());
    }

    @Override
    @Transactional
    public NotificationResponse markRead(int notificationId) {
        Notification notification = notificationRepository
                .findByIdAndRecipientId(notificationId, currentUser.id())
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        if (notification.getReadAt() == null) {
            notification.setReadAt(LocalDateTime.now());
            notification = notificationRepository.save(notification);
        }
        return NotificationResponse.from(notification);
    }

    @Override
    @Transactional
    public void markAllRead() {
        notificationRepository.markAllRead(currentUser.id(), LocalDateTime.now());
    }

    private static String cut(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return trimmed.length() <= maxLength ? trimmed : trimmed.substring(0, maxLength);
    }
}
