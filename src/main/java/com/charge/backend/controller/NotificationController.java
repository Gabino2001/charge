package com.charge.backend.controller;

import com.charge.backend.dto.NotificationDtos.NotificationResponse;
import com.charge.backend.security.CurrentUser;
import com.charge.backend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@PreAuthorize("hasRole('COACH')")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public List<NotificationResponse> list() {
        return notificationService.listForCoach(CurrentUser.id());
    }

    @GetMapping("/unread-count")
    public Map<String, Long> unreadCount() {
        return Map.of("count", notificationService.countUnread(CurrentUser.id()));
    }

    @PatchMapping("/{notificationId}/read")
    public NotificationResponse markRead(@PathVariable Long notificationId) {
        return notificationService.markRead(CurrentUser.id(), notificationId);
    }
}
