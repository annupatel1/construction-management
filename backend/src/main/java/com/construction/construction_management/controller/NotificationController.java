package com.construction.construction_management.controller;

import com.construction.construction_management.dto.NotificationCountResponse;
import com.construction.construction_management.dto.NotificationResponse;
import com.construction.construction_management.service.NotificationService;
import com.construction.construction_management.util.Pages;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public List<NotificationResponse> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return notificationService.list(Pages.of(page, size));
    }

    @GetMapping("/unread-count")
    public NotificationCountResponse unreadCount() {
        return new NotificationCountResponse(notificationService.unreadCount());
    }

    @PostMapping("/{id}/read")
    public NotificationResponse markRead(@PathVariable int id) {
        return notificationService.markRead(id);
    }

    @PostMapping("/read-all")
    public NotificationCountResponse markAllRead() {
        notificationService.markAllRead();
        return new NotificationCountResponse(0);
    }
}
