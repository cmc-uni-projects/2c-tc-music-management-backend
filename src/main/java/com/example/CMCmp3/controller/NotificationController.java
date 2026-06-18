package com.example.CMCmp3.controller;

import com.example.CMCmp3.dto.NotificationDTO;
import com.example.CMCmp3.service.NotificationService;
import com.example.CMCmp3.service.UserService; // Để lấy current user id
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<NotificationDTO>> getMyNotifications(Authentication authentication){
        Long currentUserId = userService.getMe(authentication).getId();
        return ResponseEntity.ok(notificationService.getUserNotifications(currentUserId));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok().build();
    }
}
