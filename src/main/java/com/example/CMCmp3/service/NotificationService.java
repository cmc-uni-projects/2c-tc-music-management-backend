package com.example.CMCmp3.service;

import com.example.CMCmp3.dto.NotificationDTO;
import com.example.CMCmp3.entity.Notification;
import com.example.CMCmp3.entity.NotificationType;
import com.example.CMCmp3.entity.User;
import com.example.CMCmp3.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate; // Công cụ WebSocket

    @Transactional
    public void createAndSendNotification(User sender, User recipient, NotificationType type, String message, Long refId) {
        // tự like bài mình thì thông báo
        if (sender.getId().equals(recipient.getId())) {
            return;
        }

        // Lưu vào DB
        Notification notification = Notification.builder()
                .sender(sender)
                .recipient(recipient)
                .type(type)
                .message(message)
                .referenceId(refId)
                .isRead(false)
                .build();

        Notification savedNotif = notificationRepository.save(notification);

        NotificationDTO dto = toDTO(savedNotif);

        // Gửi Real-time qua WebSocket
        messagingTemplate.convertAndSendToUser(
                recipient.getEmail(),
                "/queue/notifications",
                dto
        );
    }

    @Transactional
    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            n.setRead(true);
            notificationRepository.save(n);
        });
    }

    public List<NotificationDTO> getUserNotifications(Long userId) {
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }


    private NotificationDTO toDTO(Notification n) {
        NotificationDTO dto = new NotificationDTO();
        dto.setId(n.getId());
        dto.setSenderName(n.getSender().getDisplayName());
        dto.setSenderAvatar(n.getSender().getAvatarUrl());
        dto.setMessage(n.getMessage());
        dto.setType(n.getType().name());
        dto.setReferenceId(n.getReferenceId());
        dto.setRead(n.isRead());
        dto.setCreatedAt(n.getCreatedAt());
        return dto;
    }
}